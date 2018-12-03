package compile

import java.io._

import codegen._
import edu.mit.compilers.grammar.{DecafParser, DecafScanner, DecafScannerTokenTypes}
import ir.components._
import ir._
import optimization._
import util.CLI

import scala.Console
import scala.collection.breakOut
import scala.collection.mutable.Map
import scala.sys.process._

object Compiler {
  val tokenMap = Map(
    DecafScannerTokenTypes.CHAR_LITERAL -> "CHARLITERAL",
    DecafScannerTokenTypes.DECIMAL -> "INTLITERAL",
    DecafScannerTokenTypes.HEXADECIMAL -> "INTLITERAL",
    DecafScannerTokenTypes.SC_ID -> "IDENTIFIER",
    DecafScannerTokenTypes.STR_LITERAL -> "STRINGLITERAL",
    DecafScannerTokenTypes.TK_false -> "BOOLEANLITERAL",
    DecafScannerTokenTypes.TK_true -> "BOOLEANLITERAL"
  )
  val outFile = if (CLI.outfile == null) Console.out else new PrintStream(new FileOutputStream(CLI.outfile))

  val optAry: Array[String] = Array[String]("cse", "cp", "dce")

  def main(args: Array[String]): Unit = {
    CLI.parse(args, optAry)
    if (CLI.target == CLI.Action.SCAN) {
      scan(CLI.infile)
    } else if (CLI.target == CLI.Action.PARSE) {
      if (parse(CLI.infile) == null) {
        System.exit(1)
      }
    } else if (CLI.target == CLI.Action.INTER) {
      if (inter(CLI.infile) == null) {
        System.exit(1)
      }
    } else if (CLI.target == CLI.Action.ASSEMBLY) {
      val optFlagMap: Map[String, Boolean] = (optAry zip CLI.opts) (breakOut)
      if (assembly(CLI.infile, CLI.outfile, optFlagMap) == null) {
        System.exit(1)
      }

    }
    System.exit(0)
  }

  def scan(fileName: String, debugSwitch: Boolean = CLI.debug) {
    try {
      val inputStream: FileInputStream = new java.io.FileInputStream(fileName)
      val scanner = new DecafScanner(new DataInputStream(inputStream))
      scanner.setTrace(debugSwitch)
      if (debugSwitch) {
        println("\nPrinting debug info for scanner:\n")
        println("Scanner trace:")
      }
      var done = false
      while (!done) {
        try {
          val head = scanner.nextToken()
          if (head.getType() == DecafScannerTokenTypes.EOF) {
            done = true
          } else {
            val tokenType = tokenMap.getOrElse(head.getType(), "")
            outFile.println(head.getLine() + (if (tokenType == "") "" else " ") + tokenType + " " + head.getText())
          }
        } catch {
          case ex: Exception => {
            Console.err.println(CLI.infile + " " + ex)
            scanner.consume();
          }
        }
      }
    } catch {
      case ex: Exception => Console.err.println(ex)
    }
  }

  def parse(fileName: String, debugSwitch: Boolean = CLI.debug): ScalarAST = {
    /**
      * Parse the file specified by the filename. Eventually, this method
      * may return a type specific to your compiler.
      */
    var inputStream: java.io.FileInputStream = null
    try {
      inputStream = new java.io.FileInputStream(fileName)
    } catch {
      case f: FileNotFoundException => {
        Console.err.println("File " + fileName + " does not exist")
        return null
      }
    }

    try {
      val scanner = new DecafScanner(new DataInputStream(inputStream))
      val parser = new DecafParser(scanner);

      // convert to CommonASTWithLines: see ir.CommonASTWithLines for more info
      parser.setASTNodeClass("ir.CommonASTWithLines")
      if (debugSwitch) {
        println("\nPrinting debug info for Parser:\n")
        println("Parser trace:")
      }
      parser.setTrace(debugSwitch)
      parser.program()
      if (debugSwitch) {
        println()
      }
      val t = parser.getAST().asInstanceOf[CommonASTWithLines]
      if (parser.getError()) {
        println("[ERROR] Parse failed")
        return null
      } else if (debugSwitch) {
        println("Parser inline view:")
        println(t.toStringList)
        println()
      }

      // CommonASTWithLines to ScalarAST
      val ast = ScalarAST.fromCommonAST(t)
      if (debugSwitch) {
        println("Parser tree view:")
        ast.prettyPrint()
        println()
      }
      ast
    } catch {
      case e: Exception => Console.err.println(CLI.infile + " " + e)
        null
    }
  }

  def inter(fileName: String, debugSwitch: Boolean = CLI.debug): IR = {
    val optAST = Option(parse(fileName, false))

    // parsing failed
    if (optAST.isEmpty) {
      System.exit(1)
    }

    val ast = optAST.get

    // change AST to IR
    val ir = ASTtoIR(ast)
    if (ASTtoIR.error) {
      System.exit(1)
    }

    TypeCheck(ir)
    if (TypeCheck.error) {
      System.exit(1)
    }

    MiscCheck.apply
    if (MiscCheck.error) {
      System.exit(1)
    }

    if (debugSwitch) {
      println("\nPrinting debug info for IR:\n")
      println("IR tree view:")
      PrettyPrint(ir, 0)
      println()
    }

    ir
  }

  def assembly(inFile: String, outFile: String, optFlagMap: Map[String, Boolean], debugSwitch: Boolean = CLI.debug): IR = {
    val optIR = Option(inter(inFile, false))
    val output = if (outFile == null) None else Some(outFile)
    val str2Opts = Map[String, Map[String, Option[Optimization]]](
      "cse" -> Map[String, Option[Optimization]](
        "local" -> Option(CSE),
        "global" -> None
        ),
      "cp" -> Map[String, Option[Optimization]](
        "local" -> Option(CP),
        "global" -> Option(GlobalCP)
        ),
      "dce" -> Map[String, Option[Optimization]](
        "local" -> Option(DCE),
        "global" -> Option(GlobalDCE)
        )
    )

    // parsing failed
    if (optIR.isEmpty) {
      System.exit(1)
    }

    val ir = optIR.get
    val iter = Stream.iterate(0)(_ + 1).iterator

    val irModified = IRto3Addr(ir, iter)

    if (debugSwitch) {
      println("\nPrinting debug info for Assembly:\n")
    }

    if (debugSwitch) {
      println("Low-level IR tree:")
      PrettyPrint(irModified, 1)
      println()
    }

    val (start, end) = Destruct(irModified)

    // Begin Optimization
    if (debugSwitch) {
      println("Enabled optimizations:")
      for (opt <- optFlagMap.keys) {
        if (optFlagMap(opt)) {
          printf(opt)
          printf(" ")
        }
      }
      println()
    }

    val optCFG = PeepHole(start, preserveCritical=true).get

    val localOptPreq = Vector[Optimization]()
    val localOptCond = GenerateOptVec(str2Opts, optFlagMap, Vector("cse", "cp"), "local")
    val localOptSeq = GenerateOptVec(str2Opts, optFlagMap, Vector("dce"), "local")

    val localOptIter = RepeatOptimization(optCFG, Option(localOptPreq), localOptCond, Option(localOptSeq))

    assert(RepeatOptimization(optCFG, None, localOptSeq, None) == 1) // an extra run of DCE should not change anything

    if (debugSwitch) {
      println("Local optimizations:")
      printf("- Prequels:\n  ")
      for (opt <- localOptPreq) { printf(s"${opt} ") }
      printf("\n- Conditions:\n  ")
      for (opt <- localOptCond) { printf(s"${opt} ") }
      printf("\n- Sequels:\n  ")
      for (opt <- localOptSeq) { printf(s"${opt} ") }
      println(s"\nNumber of local optimization iterations before fixed point: ${localOptIter}")
      println()
    }

    val globalOptPreq = Vector[Optimization]()
    val globalOptCond = GenerateOptVec(str2Opts, optFlagMap, Vector("cp", "dce"), "global")
    val globalOptSeq = Vector[Optimization]()

    val globalOptIter = RepeatOptimization(optCFG, None, globalOptCond, None)
    
    if (debugSwitch) {
      println("Global optimizations:")
      printf("- Prequels:\n  ")
      for (opt <- globalOptPreq) { printf(s"${opt} ") }
      printf("\n- Conditions:\n  ")
      for (opt <- globalOptCond) { printf(s"${opt} ") }
      printf("\n- Sequels:\n  ")
      for (opt <- globalOptSeq) { printf(s"${opt} ") }
      println(s"\nNumber of global optimization iterations before fixed point: ${globalOptIter}")
      println()
    }

    // End Optimization
    Destruct.reconstruct() // reconstruct logical shortcuts

    val optCFGFinal = PeepHole(optCFG, preserveCritical=false).get

    /*
    if (debugSwitch) {
      PrintCFG.init()
      PrintCFG(optCFGFinal)
      PrintCFG.close()
    }
    */

    Allocate(optCFGFinal)

    // Deprecated
    /*
    if (debugSwitch) {
      println("Low-level IR tree after destruct, peephole and allocate:")
      PrettyPrint(irModified, 2)
      println()
    }
    */

    if (debugSwitch) {
      println("x86-64 assembly:")
    }

    TranslateCFG(optCFGFinal, output, debugSwitch)
    TranslateCFG.closeOutput

    if (debugSwitch) {
      println()
    }

    if (debugSwitch && !output.isEmpty) {
      println("Execution result:")
      val asmFileVec = outFile.split("\\.")
      val binFileVec = asmFileVec.slice(0, asmFileVec.length - 1)
      val binFile = binFileVec.mkString(".")
      val compileRet = s"gcc -o ${binFile} ${outFile} -no-pie".! // Hardened compile chain workaround
      println()
      println(s"Compilation returns ${compileRet}\n");
      if (compileRet == 0) {
        val runRet = s"${binFile}".!
        println()
        println(s"Program returns ${runRet}\n");
      }
    }

    irModified
  }
}
