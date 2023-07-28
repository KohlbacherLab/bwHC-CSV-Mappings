package de.bwhc.mtb.csv.app


import java.io.{
  File,
  FileWriter,
  FileInputStream
}
import scala.util.chaining._
import scala.util.Try
import play.api.libs.json.Json
import cats.Semigroup
import de.bwhc.mtb.data.entry.dtos.MTBFile
import de.bwhc.util.csv.Csv._
import de.bwhc.util.csv.Delimiter
import de.bwhc.util.csv.Csv.syntax._
import de.bwhc.mtb.csv.PartialMTBFile
import de.bwhc.mtb.csv.Writers._


object Main extends App
{ 

  val IN_ARG  = "-in" 
  val OUT_ARG = "-out" 

  implicit val del = Delimiter.Pipe

  {
    for {
      in  <- Try { args(args.indexOf(IN_ARG)+1) }
      out =  args(args.indexOf(OUT_ARG)+1) 
    } yield {
    
      println(s"Using JSON input directory: $in")
      println(s"Using CSV output directory: $out")
    
      val inputDir  = new File(in)
      val outputDir = new File(out)
  
      val (successes,errors) =
        inputDir
          .listFiles((_,name) => name.toLowerCase endsWith ".json")
          .to(LazyList)
          .map {
            file => 
              println(s"Processing file: ${file.getAbsolutePath}")
              
              file ->
                new FileInputStream(file)
                  .pipe(Json.parse)
                  .pipe(Json.fromJson[MTBFile](_)) 
          }
          .partition { case (_,js) => js.isSuccess }

      successes  
        .map(_._2.get)
        .map(PartialMTBFile.of)
        .pipe(splitWriteToCsv(_,outputDir))


      if (errors.nonEmpty){  
        
        val errWriter =
          new FileWriter(new File(outputDir,"errors.txt"))  
        
        errors.foreach {
          case (file,js) =>
            js.fold(
              err => {
                errWriter.write(s"Errors: ${file.getAbsolutePath}\n")
                err.foreach { 
                  case (path,errs) =>
                    errs.foreach(e => errWriter.write(s"$path: ${e.messages}\n"))
                }
              },
              _ => ()
            )
        }
        errWriter.close
        
      }

    }
    
  }
  .recover {

    case t: ArrayIndexOutOfBoundsException =>
      println(s"Usage: $IN_ARG <input dir for JSON MTBFiles> $OUT_ARG <output dir for CSV files>")

    case t =>
      t.printStackTrace
  }

  System.exit(0)

}
