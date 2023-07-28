package de.bwhc.mtb.csv


import java.time.{
  LocalDate,
  YearMonth
}
import java.time.format.DateTimeFormatter
import de.bwhc.util.csv._
import CsvWriter.temporal._
import CsvWriter.derivation._
import de.bwhc.mtb.data.entry.dtos._
import ValueSets._
import de.bwhc.catalogs.med.MedicationCatalog
import de.bwhc.catalogs.icd.{
  ICDO3Catalogs,
  ICD10GMCatalogs
}
import de.bwhc.catalogs.hgnc.HGNCCatalog



trait Writers
{

  import CodingExtensions._


  implicit val icd10catalogs =
    ICD10GMCatalogs.getInstance.get

  implicit val icdO3catalogs =
    ICDO3Catalogs.getInstance.get

  implicit val atcCatalog =
    MedicationCatalog.getInstance.get

  implicit val hgncCatalog =
    HGNCCatalog.getInstance.get


  implicit def enumDisplayWriter[E <: Enumeration](
    implicit vs: ValueSet[E#Value]
  ): CsvWriter[E#Value] =
    CsvWriter[E#Value](e => vs.displayOf(e).get)


  implicit val yearMonthWriter =
    CsvWriter.of[YearMonth](DateTimeFormatter.ofPattern("MM.yyyy"))
    

  implicit val patientWriter =
    CsvWriter.of[Patient](
      "ID"           -> CsvWriter.on((p: Patient) => p.id),
      "Geschlecht"   -> CsvWriter.on((p: Patient) => p.gender),
      "Geburtsdatum" -> CsvWriter.on((p: Patient) => p.birthDate),
      "Todesdatum"   -> CsvWriter.on((p: Patient) => p.dateOfDeath),
      "Krankenkasse" -> CsvWriter.on((p: Patient) => p.insurance)
    )


  //TODO TODO: Resolve ICD-10 display!!!
  
  implicit val diagnosisWriter =
    CsvWriter.of[Diagnosis](
      "ID"                -> CsvWriter.on((d: Diagnosis) => d.id),
      "Patient"           -> CsvWriter.on((d: Diagnosis) => d.patient),
      "Erfassungsdatum"   -> CsvWriter.on((d: Diagnosis) => d.recordedOn),
      "ICD-10 Code"       -> CsvWriter.on((d: Diagnosis) => d.icd10.map(_.code)),
      "Entität"           -> CsvWriter.on((d: Diagnosis) => d.icd10.map(_.complete.display)),
      "ICD-10 Version"    -> CsvWriter.on((d: Diagnosis) => d.icd10.flatMap(_.version)),
      "ICD-O-3 T Code"    -> CsvWriter.on((d: Diagnosis) => d.icdO3T.map(_.code)),
      "Topographie"       -> CsvWriter.on((d: Diagnosis) => d.icdO3T.map(_.complete.display)),
      "ICD-O-3 Version"   -> CsvWriter.on((d: Diagnosis) => d.icdO3T.flatMap(_.version)),
      "WHO Grad"          -> CsvWriter.on((d: Diagnosis) => d.whoGrade.map(c => s"${c.code}")),
      "Tumor-Ausbreitung" -> CsvWriter.on((d: Diagnosis) =>
                               d.statusHistory
                                .getOrElse(List.empty)
                                .maxByOption(_.date)
                                .map(_.status)
                             ),
      "Leitlinien-Status" -> CsvWriter.on((d: Diagnosis) => d.guidelineTreatmentStatus)
    )


  //TODO TODO: Resolve display!!!
  implicit val medicationListWriter =
    CsvWriter[List[Medication.Coding]](
      _.map(_.complete)
       .flatMap(_.display)
       .mkString(",")
    )


  implicit val guideLineTherapyWriter =
    CsvWriter.of[GuidelineTherapy](
      "ID"                -> CsvWriter.on((th: GuidelineTherapy) => th.id),
      "Patient"           -> CsvWriter.on((th: GuidelineTherapy) => th.patient),
      "Diagnose"          -> CsvWriter.on((th: GuidelineTherapy) => th.diagnosis),
      "Therapie-Linie"    -> CsvWriter.on((th: GuidelineTherapy) => th.therapyLine),
      "Medikation"        -> CsvWriter.on((th: GuidelineTherapy) => th.medication),
      "ATC Version"       -> CsvWriter.on((th: GuidelineTherapy) => 
                               th.medication.getOrElse(List.empty)
                                 .flatMap(_.version)
                                 .distinct
                                 .mkString(",")
                             ),
      "Anfangsdatum"      -> CsvWriter.on((t: GuidelineTherapy) =>
                               t match {
                                 case th: LastGuidelineTherapy     => th.period.map(_.start)
                                 case th: PreviousGuidelineTherapy => None
                               }
                             ),
      "Enddatum"          -> CsvWriter.on((t: GuidelineTherapy) =>
                               t match {
                                 case th: LastGuidelineTherapy     => th.period.flatMap(_.end)
                                 case th: PreviousGuidelineTherapy => None
                               }
                             ),
      "Abbruchsgrund"     -> CsvWriter.on((t: GuidelineTherapy) =>
                               t match {
                                 case th: LastGuidelineTherapy => th.reasonStopped.map(_.code)
                                 case th: PreviousGuidelineTherapy => None
                               }
                             )
    )


  implicit val ecogStatusWriter =
    CsvWriter.of[ECOGStatus](
      "ID"           -> CsvWriter.on((p: ECOGStatus) => p.id),
      "Patient"      -> CsvWriter.on((p: ECOGStatus) => p.patient),
      "Datum"        -> CsvWriter.on((p: ECOGStatus) => p.effectiveDate),
      "ECOG Status"  -> CsvWriter.on((p: ECOGStatus) => p.value.code)
    )


  implicit val specimenWriter =
    CsvWriter.of[Specimen](
      "ID"               -> CsvWriter.on((p: Specimen) => p.id),
      "Patient"          -> CsvWriter.on((p: Specimen) => p.patient),
      "ICD-10 Code"      -> CsvWriter.on((p: Specimen) => p.icd10.code),
      "Entität"          -> CsvWriter.on((p: Specimen) => p.icd10.complete.display),
      "Proben-Art"       -> CsvWriter.on((p: Specimen) => p.`type`),
      "Entnahme-Datum"   -> CsvWriter.on((p: Specimen) => p.collection.map(_.date)),
      "Lokalisierung"    -> CsvWriter.on((p: Specimen) => p.collection.map(_.localization)),
      "Entnahme-Methode" -> CsvWriter.on((p: Specimen) => p.collection.map(_.method))
    )


  implicit val somaticNGSReportWriter =
    CsvWriter.of[SomaticNGSReport](
      "ID"                 -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.id),
      "Patient"            -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.patient),
      "Probe"              -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.specimen),
      "Erstallungsdatum"   -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.issueDate),
      "Sequenzierungs-Art" -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.sequencingType),
      "Tumor-Zell-Gehalt"  -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.tumorCellContent.map(_.value)),
      "BRCAness"           -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.brcaness.map(_.value)),
      "MSI"                -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.msi.map(_.value)),
      "Mutationslast"      -> CsvWriter.on((ngs: SomaticNGSReport) => ngs.tmb.map(_.value)),
    )


  implicit val startEndRangeWriter =
    CsvWriter[Variant.StartEnd]{
      case Variant.StartEnd(start,end) =>
        s"${start}${end.map(e => s" - $e").getOrElse("")}"
    }

  implicit val geneCodingListWriter =
    CsvWriter[List[Gene.Coding]](_.flatMap(_.complete.symbol.map(_.value)).mkString(","))


  implicit val simpleVariantWithPatientWriter =
    CsvWriter.of[(Patient,SimpleVariant)](
      "ID"             -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.id },
      "Patient"        -> CsvWriter.on[(Patient,SimpleVariant)]{ case (p,_) => p.id },
      "Chromosome"     -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.chromosome },
      "Gene"           -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.gene.map(_.complete.symbol) },
      "Position"       -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.startEnd },
      "Ref. Allele"    -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.refAllele },
      "Alt. Allele"    -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.altAllele },
      "DNA Change"     -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.dnaChange.map(_.code) },
      "Protein Change" -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.aminoAcidChange.map(_.code) },
      "Read Depth"     -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.readDepth },
      "Allelic Freq."  -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.allelicFrequency },
      "COSMIC ID"      -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.cosmicId },
      "dbSNP ID"       -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.dbSNPId },
      "Interpretation" -> CsvWriter.on[(Patient,SimpleVariant)]{ case (_,snv) => snv.interpretation }
    )


  implicit val copyNumberVariantWithPatientWriter =
    CsvWriter.of[(Patient,CNV)](
      "ID"                      -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.id },
      "Patient"                 -> CsvWriter.on[(Patient,CNV)]{ case (p,_) => p.id },
      "Chromosome"              -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.chromosome },
      "Start-Bereich"           -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.startRange },
      "End-Bereich"             -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.endRange },
      "Copy Number"             -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.totalCopyNumber },
      "Rel. Copy Number"        -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.relativeCopyNumber },
      "CN A"                    -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.cnA },
      "CN B"                    -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.cnB },
      "Reported Affected Genes" -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.reportedAffectedGenes },
      "Reported Focality"       -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.reportedFocality },
      "Type         "           -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.`type` },
      "Copy Number Neutral LoH" -> CsvWriter.on[(Patient,CNV)]{ case (_,cnv) => cnv.copyNumberNeutralLoH }
    )


  implicit val therapyRecommendationWriter =
    CsvWriter.of[TherapyRecommendation](
      "ID"                    -> CsvWriter.on((th: TherapyRecommendation) => th.id),
      "Patient"               -> CsvWriter.on((th: TherapyRecommendation) => th.patient),
      "Diagnose"              -> CsvWriter.on((th: TherapyRecommendation) => th.diagnosis),
      "Erstallungsdatum"      -> CsvWriter.on((th: TherapyRecommendation) => th.issuedOn),
      "Medikation"            -> CsvWriter.on((th: TherapyRecommendation) => th.medication),
      "Priorität"             -> CsvWriter.on((th: TherapyRecommendation) => th.priority),
      "Evidenz-Level"         -> CsvWriter.on((th: TherapyRecommendation) =>
                                   th.levelOfEvidence.map(_.grading.code.toString)
                                 ),
      "Evidenz-Level-Zusätze" -> CsvWriter.on((th: TherapyRecommendation) =>
                                   th.levelOfEvidence
                                     .flatMap(_.addendums)
                                     .map(_.map(_.code))
                                     .map(_.mkString(","))
                                 ),
//      "NGS-Bericht"            -> CsvWriter.on((th: TherapyRecommendation) => th.ngsReport),
//      "Stützende Alterationen" -> CsvWriter.on((th: TherapyRecommendation) => th.supportingVariants),
    ) 


  implicit val molecularTherapyWriter =
    CsvWriter.of[MolecularTherapy](
      "ID"                     -> CsvWriter.on((th: MolecularTherapy) => th.id),
      "Patient"                -> CsvWriter.on((th: MolecularTherapy) => th.patient),
      "Empfehlung"             -> CsvWriter.on((th: MolecularTherapy) => th.basedOn),
      "Erfassungsdatum"        -> CsvWriter.on((th: MolecularTherapy) => th.recordedOn),
      "Status"                 -> CsvWriter.on((th: MolecularTherapy) => th.status),
      "Anfangsdatum"           -> CsvWriter.on((th: MolecularTherapy) => th.period.map(_.start)),
      "Enddatum"               -> CsvWriter.on((th: MolecularTherapy) =>
                                    th.period.flatMap {
                                      case p: OpenEndPeriod[LocalDate] => p.end
                                      case p: ClosedPeriod[LocalDate]  => Some(p.end)
                                    }
                                  ),
      "Medikation"             -> CsvWriter.on((th: MolecularTherapy) => th.medication),
      "ATC Version"            -> CsvWriter.on((th: MolecularTherapy) => 
                                    th.medication.getOrElse(List.empty)
                                      .flatMap(_.version)
                                      .distinct
                                      .mkString(",")
                                  ),
      "Dosis-Dichte"           -> CsvWriter.on((th: MolecularTherapy) => th.dosage.map(_.toString)),
      "Nicht-Umsetzungs-Grund" -> CsvWriter.on((th: MolecularTherapy) => th.notDoneReason.map(_.code)),
      "Abbruchsgrund"          -> CsvWriter.on((th: MolecularTherapy) => th.reasonStopped.map(_.code)),
      "Anmerkungen"            -> CsvWriter.on((th: MolecularTherapy) => th.note)
    )


  implicit val responseWriter =
    CsvWriter.of[Response](
      "ID"       -> CsvWriter.on((p: Response) => p.id),
      "Patient"  -> CsvWriter.on((p: Response) => p.patient),
      "Therapie" -> CsvWriter.on((p: Response) => p.therapy),
      "Datum"    -> CsvWriter.on((p: Response) => p.effectiveDate),
      "Ergebnis" -> CsvWriter.on((p: Response) => p.value.code),
    )


}

object Writers extends Writers

