// $Id$

package scala.continuations

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

class SelectiveCPSTransformPlugin(val global: Global) extends Plugin {
  import global._

  val name = "selectivecps"
  val description = "applies selective cps conversion"
  val components = List[PluginComponent](
    new SelectiveANFTransform() { 
      val global = SelectiveCPSTransformPlugin.this.global
      val runsAfter = "typer" 
    }, 
    new SelectiveCPSTransform() { 
      val global = SelectiveCPSTransformPlugin.this.global
      val runsAfter = "selectiveanf"
    }
  )

}
