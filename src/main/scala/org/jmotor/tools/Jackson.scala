package org.jmotor.tools

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.jmotor.tools.dto.Artifact

object Jackson {

  val mapper: ObjectMapper = JsonMapper
    .builder()
    .addModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .build()

  val tr: TypeReference[Seq[Artifact]] = new TypeReference[Seq[Artifact]] {}

}
