package coursemgmt.support

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

trait ExtractUniquePathsFixture {

  val nonOverlappingPaths: List[String] =
    List("config", "src/test", "otherSrc/test", "README.md", "docs/i.png")

  val overlappingPaths: List[String] =
    List("config", "src/test", "src/test/cmt/MySpec.scala", "README.md", "config/example-1.yaml")
}

final class ExtractUniquePathsSpec extends AnyWordSpec with Matchers with ExtractUniquePathsFixture {

  import coursemgmt.Helpers.extractUniquePaths

  "extractUniquePaths" when {
    "given a series of paths that don't share any common prefix" should {
      "return no redundant paths" in {
        val l = nonOverlappingPaths.permutations
        l.foreach { paths =>
          val (uniquePaths, redundantPaths) = extractUniquePaths(paths)
          val (actualUniquePaths, actualRedundantPaths) = (uniquePaths.to(Set), redundantPaths.to(Set))

          val (expectedUniquePaths, expectedRedundantPaths) = (nonOverlappingPaths.to(Set), Set.empty)

          assert(actualUniquePaths == expectedUniquePaths)
          assert(actualRedundantPaths == expectedRedundantPaths)
        }
      }
    }

    "given a series of paths where some share a common prefix" should {
      "return redundant paths" in {
        val l = overlappingPaths.permutations
        l.foreach { paths =>
          val (uniquePaths, redundantPaths) = extractUniquePaths(paths)
          val (actualUniquePaths, actualRedundantPaths) = (uniquePaths.to(Set), redundantPaths.to(Set))

          val (expectedUniquePaths, expectedRedundantPaths) =
            (Set("src/test", "config", "README.md"), Set("src/test/cmt/MySpec.scala", "config/example-1.yaml"))

          assert(actualUniquePaths == expectedUniquePaths)
          assert(actualRedundantPaths == expectedRedundantPaths)
        }
      }
    }
  }
}
