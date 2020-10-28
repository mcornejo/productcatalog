package com.murdix.bm.entities

/* A class to represent a (line) of ProductCatalog */
case class ProductCatalog(
                           brand: Option[String],
                           category_id: Option[Long],
                           comment: Option[String],
                           currency: Option[String],
                           description: Option[String],
                           image: Option[String],
                           year_release: Option[Int]) {

  def isComplete: Boolean = {
    image.exists(_.contains("data:image")) && currency.exists(!_.isEmpty)
  }

  def isValid: Boolean = {
    image.exists(_.contains("data:image"))
  }
}
