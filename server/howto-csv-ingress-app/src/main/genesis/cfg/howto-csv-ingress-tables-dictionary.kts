/**
  * This file defines the entities (or tables) for the application.  
  * Entities aggregation a selection of the universe of fields defined in 
  * {app-name}-fields-dictionary.kts file into a business entity.  
  *
  * Note: indices defined here control the APIs available to the developer.
  * For example, if an entity requires lookup APIs by one or more of its attributes, 
  * be sure to define either a unique or non-unique index.

  * Full documentation on tables may be found here >> https://docs.genesis.global/docs/develop/server-capabilities/data-model/

 */

tables {
  table(name = "EXCHANGE_HOLIDAY", id = 11_000, audit = details(id = 11_500, sequence = "EH")) {
    field("HOLIDAY_DATE", DATE).notNull()
    field("HOLIDAY_NAME", STRING)
    field("HOLIDAY_TYPE", ENUM("Settlement","Trading")).default("Settlement").notNull()
    field("ISO_MIC", STRING).notNull()

    primaryKey("HOLIDAY_TYPE","ISO_MIC","HOLIDAY_DATE").name("EXCHANGE_HOLIDAY_BY_HOLIDAY_TYPE_ISO_MIC_HOLIDAY_DATE")
  }
  table(name = "EXCHANGE", id = 11_001, audit = details(id = 11_501, sequence = "EA")) {
    field("ISO_COUNTRY", STRING).notNull()
    field("ISO_MIC", STRING).notNull()
    field("NAME", STRING(200)).notNull()

    primaryKey("ISO_MIC")
  }
}
