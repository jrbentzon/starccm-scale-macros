// STAR-CCM+ macro: createRegion.java
// Written by STAR-CCM+ 14.06.013
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;

public class createRegion extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation = 
      getActiveSimulation();

    Region fluidRegion = 
      simulation.getRegionManager().createEmptyRegion();

    fluidRegion.setPresentationName("Fluid");

    Boundary boundary_0 = 
      fluidRegion.getBoundaryManager().getBoundary("Default");

    boundary_0.setPresentationName("Stator");

    Boundary boundary_1 = 
      fluidRegion.getBoundaryManager().createEmptyBoundary();

    boundary_1.setPresentationName("Rotor");

    Boundary boundary_2 = 
      fluidRegion.getBoundaryManager().createEmptyBoundary();

    boundary_2.setPresentationName("InletA");

    InletBoundary inletBoundary_0 = 
      ((InletBoundary) simulation.get(ConditionTypeManager.class).get(InletBoundary.class));

    boundary_2.setBoundaryType(inletBoundary_0);

    Boundary boundary_3 = 
      fluidRegion.getBoundaryManager().createEmptyBoundary();

    boundary_3.setPresentationName("OutletA");

    OutletBoundary outletBoundary_0 = 
      ((OutletBoundary) simulation.get(ConditionTypeManager.class).get(OutletBoundary.class));

    boundary_3.setBoundaryType(outletBoundary_0);
  }
}
