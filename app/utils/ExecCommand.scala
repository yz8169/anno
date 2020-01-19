package utils

import java.io.File

import scala.concurrent.Future
import scala.sys.process
import scala.sys.process._

class ExecCommand {
  var isSuccess: Boolean = false
  val err = new StringBuilder
  val out = new StringBuilder
  val log = ProcessLogger(out append _ append "\n", err append _ append "\n")


  def exec(command: String) = {
    val exitCode = command ! log
    if (exitCode == 0) isSuccess = true
  }

  def exec(command1: String, command2: String) = {
    val exitCode = (command1 #&& command2) ! log
    if (exitCode == 0) isSuccess = true
  }

  def exec(command: String, outFile: File, workspace: File) = {
    val process = Process(command, workspace).#>(outFile)
    val exitCode = process ! log
    if (exitCode == 0) isSuccess = true
  }

  def exec(command: String, workspace: File) = {
    val process = Process(command, workspace)
    val exitCode = process ! log
    if (exitCode == 0) isSuccess = true
  }

  def exec(command1: String, command2: String, workspace: File) = {
    val exitCode = Process(command1, workspace).#&&(Process(command2, workspace)) ! log
    if (exitCode == 0) isSuccess = true
  }

  def exec(command1: String, command2: String,command3:String, workspace: File) = {
    val exitCode = Process(command1, workspace).#&&(Process(command2, workspace)).#&&(Process(command3, workspace)) ! log
    if (exitCode == 0) isSuccess = true
  }

  def execPar( workspace: File,commands: String*) = {
    val exitCodes=commands.par.map{command=>
      Process(command, workspace) ! log
    }
    if (exitCodes.forall(_==0)) isSuccess = true
  }

  def execWithWorkspace(command: String, workspace: File) = {
    val exitCode = Process(command, workspace) ! log
    if (exitCode == 0) isSuccess = true
  }

  def execWithWorkspace(command1: String, command2: String, command4: String, workspace: File) = {
    val process = Process(command1, workspace).#&&(Process(command2, workspace)).#&&(Process(command4, workspace))
    val exitCode = process ! log
    if (exitCode == 0) isSuccess = true
  }

  def execWithWorkspace(command1: String, command2: String, workspace: File, rCommand: String, rWorkspace: File) = {
    val process = Process(command1, workspace).#&&(Process(command2, workspace))
    val exitCode = process.#&&(Process(rCommand, rWorkspace)) ! log
    if (exitCode == 0) isSuccess = true
  }

  def execWithWorkspace(command3: String, command4: String, workspace: File, command4Workspace: File) = {
    val process = Process(command3, workspace).#&&(Process(command4, command4Workspace))
    val exitCode = process ! log
    if (exitCode == 0) isSuccess = true
  }

  def execWithWorkspace(command3: String, workspace: File, rCommand1: String, rCommand2: String, rCommand3: String,
                        rWorkspace: File) = {
    val process = Process(command3, workspace).#&&(Process(rCommand1, rWorkspace)).
      #&&(Process(rCommand2, rWorkspace)).#&&(Process(rCommand3, rWorkspace))
    val exitCode = process ! log
    if (exitCode == 0) isSuccess = true
  }

  def execWithWorkspace1(command1: String, command2: String, command3: String, workspace: File, command3Workspace: File) = {
    val process = Process(command1, workspace).###(Process(command2, workspace))
    val exitCode = process.#&&(Process(command3, command3Workspace)) ! log
    if (exitCode == 0) isSuccess = true
  }


  def getErrStr = err.toString()


  def getOutStr = out.toString()

}
