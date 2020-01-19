package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import com.github.tototoshi.slick.MySQLJodaSupport._
  import org.joda.time.DateTime
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Mission.schema ++ Sample.schema ++ User.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Mission
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param missionname Database column missionName SqlType(VARCHAR), Length(255,true)
   *  @param userid Database column userId SqlType(INT)
   *  @param samplename Database column sampleName SqlType(VARCHAR), Length(255,true)
   *  @param starttime Database column startTime SqlType(DATETIME)
   *  @param endtime Database column endTime SqlType(DATETIME), Default(None)
   *  @param state Database column state SqlType(VARCHAR), Length(255,true) */
  final case class MissionRow(id: Int, missionname: String, userid: Int, samplename: String, starttime: DateTime, endtime: Option[DateTime] = None, state: String)
  /** GetResult implicit for fetching MissionRow objects using plain SQL queries */
  implicit def GetResultMissionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime], e3: GR[Option[DateTime]]): GR[MissionRow] = GR{
    prs => import prs._
    MissionRow.tupled((<<[Int], <<[String], <<[Int], <<[String], <<[DateTime], <<?[DateTime], <<[String]))
  }
  /** Table description of table mission. Objects of this class serve as prototypes for rows in queries. */
  class Mission(_tableTag: Tag) extends profile.api.Table[MissionRow](_tableTag, Some("anno"), "mission") {
    def * = (id, missionname, userid, samplename, starttime, endtime, state) <> (MissionRow.tupled, MissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(missionname), Rep.Some(userid), Rep.Some(samplename), Rep.Some(starttime), endtime, Rep.Some(state)).shaped.<>({r=>import r._; _1.map(_=> MissionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column missionName SqlType(VARCHAR), Length(255,true) */
    val missionname: Rep[String] = column[String]("missionName", O.Length(255,varying=true))
    /** Database column userId SqlType(INT) */
    val userid: Rep[Int] = column[Int]("userId")
    /** Database column sampleName SqlType(VARCHAR), Length(255,true) */
    val samplename: Rep[String] = column[String]("sampleName", O.Length(255,varying=true))
    /** Database column startTime SqlType(DATETIME) */
    val starttime: Rep[DateTime] = column[DateTime]("startTime")
    /** Database column endTime SqlType(DATETIME), Default(None) */
    val endtime: Rep[Option[DateTime]] = column[Option[DateTime]]("endTime", O.Default(None))
    /** Database column state SqlType(VARCHAR), Length(255,true) */
    val state: Rep[String] = column[String]("state", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table Mission */
  lazy val Mission = new TableQuery(tag => new Mission(tag))

  /** Entity class storing rows of table Sample
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param userid Database column userId SqlType(INT)
   *  @param samplename Database column sampleName SqlType(VARCHAR), Length(255,true)
   *  @param describe Database column describe SqlType(VARCHAR), Length(255,true)
   *  @param max Database column max SqlType(INT)
   *  @param min Database column min SqlType(INT)
   *  @param mean Database column mean SqlType(INT)
   *  @param size Database column size SqlType(INT)
   *  @param kind Database column kind SqlType(VARCHAR), Length(255,true)
   *  @param uploadtime Database column uploadTime SqlType(DATETIME) */
  final case class SampleRow(id: Int, userid: Int, samplename: String, describe: String, max: Int, min: Int, mean: Int, size: Int, kind: String, uploadtime: DateTime)
  /** GetResult implicit for fetching SampleRow objects using plain SQL queries */
  implicit def GetResultSampleRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime]): GR[SampleRow] = GR{
    prs => import prs._
    SampleRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[DateTime]))
  }
  /** Table description of table sample. Objects of this class serve as prototypes for rows in queries. */
  class Sample(_tableTag: Tag) extends profile.api.Table[SampleRow](_tableTag, Some("anno"), "sample") {
    def * = (id, userid, samplename, describe, max, min, mean, size, kind, uploadtime) <> (SampleRow.tupled, SampleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid), Rep.Some(samplename), Rep.Some(describe), Rep.Some(max), Rep.Some(min), Rep.Some(mean), Rep.Some(size), Rep.Some(kind), Rep.Some(uploadtime)).shaped.<>({r=>import r._; _1.map(_=> SampleRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column userId SqlType(INT) */
    val userid: Rep[Int] = column[Int]("userId")
    /** Database column sampleName SqlType(VARCHAR), Length(255,true) */
    val samplename: Rep[String] = column[String]("sampleName", O.Length(255,varying=true))
    /** Database column describe SqlType(VARCHAR), Length(255,true) */
    val describe: Rep[String] = column[String]("describe", O.Length(255,varying=true))
    /** Database column max SqlType(INT) */
    val max: Rep[Int] = column[Int]("max")
    /** Database column min SqlType(INT) */
    val min: Rep[Int] = column[Int]("min")
    /** Database column mean SqlType(INT) */
    val mean: Rep[Int] = column[Int]("mean")
    /** Database column size SqlType(INT) */
    val size: Rep[Int] = column[Int]("size")
    /** Database column kind SqlType(VARCHAR), Length(255,true) */
    val kind: Rep[String] = column[String]("kind", O.Length(255,varying=true))
    /** Database column uploadTime SqlType(DATETIME) */
    val uploadtime: Rep[DateTime] = column[DateTime]("uploadTime")
  }
  /** Collection-like TableQuery object for table Sample */
  lazy val Sample = new TableQuery(tag => new Sample(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param account Database column account SqlType(VARCHAR), Length(255,true)
   *  @param password Database column password SqlType(VARCHAR), Length(255,true)
   *  @param registertime Database column registerTime SqlType(DATETIME) */
  final case class UserRow(id: Int, account: String, password: String, registertime: DateTime)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[DateTime]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("anno"), "user") {
    def * = (id, account, password, registertime) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(account), Rep.Some(password), Rep.Some(registertime)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column account SqlType(VARCHAR), Length(255,true) */
    val account: Rep[String] = column[String]("account", O.Length(255,varying=true))
    /** Database column password SqlType(VARCHAR), Length(255,true) */
    val password: Rep[String] = column[String]("password", O.Length(255,varying=true))
    /** Database column registerTime SqlType(DATETIME) */
    val registertime: Rep[DateTime] = column[DateTime]("registerTime")
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
}
