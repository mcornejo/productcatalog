package com.murdix.bm.parser

import com.murdix.bm.entities.ProductCatalog

import scala.util.Try

object ProductCatalogParser {

  val parser: String => Option[ProductCatalog] = (line: String) => {
    Try {
      val tokenizedLined: Array[String] = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
        .map(_.trim)
        .map(_.replaceAll("\"", ""))

      ProductCatalog(
        brand = Try(tokenizedLined(0)).toOption.filter(_.trim.nonEmpty),
        category_id = Try(tokenizedLined(1).toLong).toOption,
        comment = Try(tokenizedLined(2)).toOption.filter(_.trim.nonEmpty),
        currency = Try(tokenizedLined(3)).toOption.filter(_.trim.nonEmpty),
        description = Try(tokenizedLined(4)).toOption.filter(_.trim.nonEmpty),
        image = Try(tokenizedLined(5)).toOption.filter(_.trim.nonEmpty),
        year_release = Try(tokenizedLined(6).toInt).toOption,
      )
    }.toOption
  }
}
