package org.jmotor.tools.dto

/**
 * Component:
 * Description:
 * Date: 15/9/17
 *
 * @author Andy.Ai
 */
case class MavenSearchRequest(groupId: Option[String], artifactId: Option[String],
                              version: Option[String], tags: Option[String] = None,
                              delimiter: String = " AND ", rows: Int = 20, start: Int = 0,
                              core: String = "gav", wt: String = "json") {
  def toParameter: String = {
    val parameters: String = List(
      groupId.map(v ⇒ s"""g:"$v"""").getOrElse(""),
      artifactId.map(v ⇒ s"""a:"$v"""").getOrElse(""),
      version.map(v ⇒ s"""v:"$v"""").getOrElse(""),
      tags.map(v ⇒ s"""tags:"$v"""").getOrElse("")).filter(v ⇒ !v.isEmpty).reduce(_ + delimiter + _)
    s"q=$parameters&core=$core&rows=$rows&wt=$wt&start=$start"
  }
}
