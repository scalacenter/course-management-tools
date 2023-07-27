package com.lunatech.cmt

import java.util.Map.Entry
import scala.jdk.CollectionConverters.*
import com.typesafe.config.{ConfigValue, ConfigFactory, ConfigObject}
final case class FileMetadata(size: Long, sha256: String)
extension (item: Entry[String, ConfigValue])
  def getInt(key: String): Int = ConfigFactory.parseString(item.getValue().unwrapped().toString()).getInt(key)
  def getString(key: String): String = ConfigFactory.parseString(item.getValue().unwrapped().toString()).getString(key)

def exMetadata(files: java.util.List[? <: ConfigObject]): Map[String, FileMetadata] =
  (for {
    item <- files.asScala
    itemitem = item.entrySet().asScala.head
    key = itemitem.getKey()
    size = itemitem.getInt("size")
    sha = itemitem.getString("sha256")
    md = FileMetadata(size, sha)
  } yield key -> md).to(Map)
