package de.bwhc.mtb.csv


import de.bwhc.mtb.data.entry.dtos._



final case class PartialMTBFile
(
  patient: Patient,
  diagnoses: Seq[Diagnosis],
  guidelineTherapies: Seq[GuidelineTherapy],
  ecogStatus: Seq[ECOGStatus],
  specimens: Seq[Specimen],
  ngsReports: Seq[SomaticNGSReport],
  simpleVariants: Seq[(Patient,SimpleVariant)],
  copyNumberVariants: Seq[(Patient,CNV)],
//  simpleVariants: Seq[SimpleVariant],
//  copyNumberVariants: Seq[CNV],
  recommendations: Seq[TherapyRecommendation],
  molecularTherapies: Seq[MolecularTherapy],
  responses: Seq[Response]
)

object PartialMTBFile
{

  def of(mtbfile: MTBFile) = {

    val ngsReports =
      mtbfile.ngsReports.getOrElse(List.empty)

    PartialMTBFile(
      mtbfile.patient,
      mtbfile.diagnoses.getOrElse(List.empty),
      mtbfile.previousGuidelineTherapies
        .getOrElse(List.empty)
        .concat[GuidelineTherapy](
          mtbfile.lastGuidelineTherapies.getOrElse(List.empty)
        ),
      mtbfile.ecogStatus.getOrElse(List.empty),
      mtbfile.specimens.getOrElse(List.empty),
      ngsReports,
      ngsReports.flatMap(
        _.simpleVariants.getOrElse(List.empty)
         .map(mtbfile.patient -> _)
      ),
      ngsReports.flatMap(
        _.copyNumberVariants.getOrElse(List.empty)
         .map(mtbfile.patient -> _)
      ),
//      ngsReports.flatMap(_.simpleVariants.getOrElse(List.empty)),
//      ngsReports.flatMap(_.copyNumberVariants.getOrElse(List.empty)),
      mtbfile.recommendations.getOrElse(List.empty),
      mtbfile.molecularTherapies.getOrElse(List.empty).flatMap(_.history),
      mtbfile.responses.getOrElse(List.empty)

    )
  }

}
