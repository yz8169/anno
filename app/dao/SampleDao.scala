package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import models.Tables._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by yz on 2018/4/17
  */
class SampleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def selectBySampleName(userId:Int,sampleName: String): Future[Option[SampleRow]] = db.run(Sample.
    filter(_.userid===userId).filter(_.samplename === sampleName).result.headOption)

  def insert(row: SampleRow): Future[Unit] = db.run(Sample += row).map(_ => ())

  def selectAll(userId:Int): Future[Seq[SampleRow]] = db.run(Sample.filter(_.userid===userId).result)

  def deleteById(id: Int): Future[Unit] = db.run(Sample.filter(_.id === id).delete).map(_ => ())

  def deleteByUserId(userId: Int): Future[Unit] = db.run(Sample.filter(_.userid === userId).delete).map(_ => ())

  def selectById(id: Int): Future[SampleRow] = db.run(Sample.
    filter(_.id === id).result.head)

  def update(row:SampleRow): Future[Unit] = db.run(Sample.filter(_.id === row.id).
    update(row)).map(_ => ())

}
