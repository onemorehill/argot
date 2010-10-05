package org.clapper.argot
import java.io.File
import scala.math

object ArgotTest
{
    def main(args: Array[String])
    {
        import ArgotConverters._

        val parser = new ArgotParser(
            "test",
            preUsage=Some("ArgotTest: Version 0.1. Copyright (c) " +
                          "2010, Brian M. Clapper. Pithy quotes go here.")
        )

        val iterations = parser.option[Int](List("i", "iterations"), "n",
                                            "Total iterations")
        val verbose = parser.flag[Int](List("v", "verbose"),
                                       List("q", "quiet"),
                                       "Increment (-v, --verbose) or " +
                                       "decrement (-q, --quiet) the " +
                                       "verbosity level.")
        {
            (onOff, opt) =>

            val currentValue = opt.value.getOrElse(0)
            if (onOff) currentValue + 1 else currentValue - 1
            math.min(0, currentValue)
        }

        val noError = parser.flag[Boolean](List("n", "noerror"),
                                           "Do not abort on error.")
        val email = parser.multiOption[String](List("e", "email"), "emailaddr",
                                               "Addresses to email results")
        {
            (s, opt) =>

            val ValidAddress = """^[^@]+@[^@]+\.[a-zA-Z]+$""".r
            ValidAddress.findFirstIn(s) match
            {
                case None    => parser.usage("Bad email address \"" + s +
                                             "\" for " + opt.name + " option.")
                case Some(_) => s
            }
        }

        val output = parser.parameter[String]("outputfile",
                                              "Output file to which to write.",
                                              false)

        val input = parser.multiParameter[File]("input",
                                                "Input files to read. If not " +
                                                "specified, use stdin.",
                                                true)
        {
            (s, opt) =>

            val file = new File(s)
            if (! file.exists)
                parser.usage("Input file \"" + s + "\" does not exist.")

            file
        }

        try
        {
            parser.parse(args)
            println("----------")
            println("iterations=" + iterations.value)
            println("verbose=" + verbose.value)
            println("email=" + email.value)
            println("output=" + output.value)
            println("input=" + input.value)
        }

        catch
        {
            case e: ArgotUsageException => println(e.message)
        }
    }
}