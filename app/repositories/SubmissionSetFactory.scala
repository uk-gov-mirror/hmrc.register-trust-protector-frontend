/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repositories

import javax.inject.Inject
import mapping.register.ProtectorsMapper
import models._
import pages.register.TrustHasProtectorYesNoPage
import play.api.i18n.Messages
import play.api.libs.json.{JsNull, JsValue, Json}
import utils.RegistrationProgress
import utils.answers.{BusinessProtectorAnswersHelper, IndividualProtectorAnswersHelper}
import viewmodels.{AnswerRow, AnswerSection}

class SubmissionSetFactory @Inject()(registrationProgress: RegistrationProgress,
                                     protectorsMapper: ProtectorsMapper,
                                     individualProtectorAnswersHelper: IndividualProtectorAnswersHelper,
                                     businessProtectorAnswerHelper: BusinessProtectorAnswersHelper) {

  def createFrom(userAnswers: UserAnswers)(implicit messages: Messages): RegistrationSubmission.DataSet = {
    val status = registrationProgress.protectorsStatus(userAnswers)

    RegistrationSubmission.DataSet(
      Json.toJson(userAnswers),
      status,
      mappedDataIfCompleted(userAnswers, status),
      answerSectionsIfCompleted(userAnswers, status)
    )
  }

  private def mappedPieces(protectorsJson: JsValue) =
    List(RegistrationSubmission.MappedPiece("trust/entities/protectors", protectorsJson))

  private def mappedDataIfCompleted(userAnswers: UserAnswers, status: Option[Status]) = {
    if (status.contains(Status.Completed)) {
      protectorsMapper.build(userAnswers) match {
        case Some(protectors) => mappedPieces(Json.toJson(protectors))
        case _ => mappedPieces(JsNull)
      }
    } else {
      mappedPieces(JsNull)
    }
  }

  def answerSectionsIfCompleted(userAnswers: UserAnswers, status: Option[Status])
                               (implicit messages: Messages): List[RegistrationSubmission.AnswerSection] = {

    val trustHasProtectorYesNo = userAnswers.get(TrustHasProtectorYesNoPage).contains(true)

    if (status.contains(Status.Completed) && trustHasProtectorYesNo) {

      val entitySections = List(
        individualProtectorAnswersHelper.protectors(userAnswers),
        businessProtectorAnswerHelper.protectors(userAnswers)
      ).flatten.flatten

      val updatedFirstSection = AnswerSection(
        entitySections.head.headingKey,
        entitySections.head.rows,
        Some(Messages("answerPage.section.protectors.heading"))
      )

      val updatedSections = updatedFirstSection :: entitySections.tail

      updatedSections.map(convertForSubmission)

    } else {
      List.empty
    }
  }

  private def convertForSubmission(row: AnswerRow): RegistrationSubmission.AnswerRow = {
    RegistrationSubmission.AnswerRow(row.label, row.answer.toString, row.labelArg)
  }

  private def convertForSubmission(section: AnswerSection): RegistrationSubmission.AnswerSection = {
    RegistrationSubmission.AnswerSection(section.headingKey, section.rows.map(convertForSubmission), section.sectionKey)
  }
}
