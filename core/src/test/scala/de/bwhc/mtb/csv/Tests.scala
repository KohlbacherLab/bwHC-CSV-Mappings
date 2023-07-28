package de.bwhc.mtb.csv


import java.nio.file.Files.createTempDirectory
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
import scala.util.chaining._
import cats.Semigroup
import de.ekut.tbi.generators.Gen
import de.bwhc.util.csv._
import de.bwhc.mtb.data.gens._
import de.bwhc.mtb.data.entry.dtos._
import Writers._
import Csv.syntax._



class Tests extends AnyFlatSpec
{

  implicit val mapCombiner: Semigroup[Map[String,(CsvValue,Seq[CsvValue])]] =
    Semigroup.instance(
      (m1,m2) =>
        (m1.toSeq ++ m2.toSeq).
        groupMapReduce(_._1)(_._2){ case ((h1 -> s1),(h2 -> s2)) => (h1 -> (s1 ++ s2))}
    )


  implicit val rnd = new Random(42)

  implicit val del = Delimiter.Pipe


  val mtbfiles =
    List.fill(20)(Gen.of[MTBFile].next)


  "Writing Data to CSV" must "have worked" in {

    val outdir = createTempDirectory("csv_tests").toFile

    mtbfiles
      .map(PartialMTBFile.of)
      .map(Csv.splitToCsv(_))
      .reduce(mapCombiner.combine)
      .pipe(Csv.writeTo(_,outdir))

  }


}
