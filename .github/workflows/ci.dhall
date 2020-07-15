let GithubActions =
      https://raw.githubusercontent.com/regadas/github-actions-dhall/master/package.dhall sha256:06e487b9ad805b241c3f01a09a3fddb4db1eb2b51f95cda2c99f63ad56dd607e

let java = [ "8", "11" ]

let setup =
      [ GithubActions.steps.checkout
      , GithubActions.steps.run
          { run =
              ''
              shasum build.sbt \
                project/plugins.sbt \
                project/build.properties > gha.cache.tmp
              ''
          }
      , GithubActions.steps.cache
          { path =
              ''
              ~/.sbt
              "~/.cache/coursier"
              ''
          , key = "sbt"
          , hashFile = "gha.cache.tmp"
          }
      , GithubActions.steps.olafurpg/java-setup
          { java-version = "\${{matrix.java}}" }
      ]

let job =
      λ(steps : List GithubActions.Step.Type) →
        GithubActions.Job::{
        , strategy = Some GithubActions.Strategy::{
          , matrix = toMap { java = [ "8" ] }
          }
        , runs-on = GithubActions.types.RunsOn.ubuntu-latest
        , steps = setup # steps
        }

let checks =
      job
        [ GithubActions.steps.run
            { run =
                "sbt \"; scalafmtCheckAll; scalafmtSbtCheck\" \"; scalafixEnable; scalafix --check; test:scalafix --check\""
            }
        ]

let test =
        job
          [ GithubActions.steps.run
              { run = "sbt \"++\${{matrix.scala}} test\"" }
          ]
      ⫽ { strategy = Some GithubActions.Strategy::{
          , matrix = toMap { java, scala = [ "2.12.12", "2.13.1" ] }
          }
        }

let testWithCoverageReport =
      job
        [ GithubActions.steps.run
            { run = "sbt coverage clean test coverageReport" }
        , GithubActions.steps.run
            { run = "bash <(curl -s https://codecov.io/bash)" }
        ]

let mimaReport =
        job
          [ GithubActions.steps.run
              { run = "sbt \"++\${{matrix.scala}} mimaReportBinaryIssues\"" }
          ]
      ⫽ { strategy = Some GithubActions.Strategy::{
          , matrix = toMap { java, scala = [ "2.11.12", "2.12.12" ] }
          }
        }

in  GithubActions.Workflow::{
    , name = "ci"
    , on = GithubActions.On::{
      , push = Some GithubActions.Push::{=}
      , pull_request = Some GithubActions.PullRequest::{=}
      }
    , jobs = toMap { checks, test, testWithCoverageReport, mimaReport }
    }
