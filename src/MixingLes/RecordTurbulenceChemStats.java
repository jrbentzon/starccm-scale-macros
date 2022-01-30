// STAR-CCM+ macro: CreateTurbKineticEnergyFunctions.java
// Written by STAR-CCM+ 15.02.007
package macro;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import star.common.*;
import star.base.neo.*;
import star.base.report.*;

public class RecordTurbulenceChemStats extends StarMacro {

  String uid;
  String SimName;
  String SessionDirectory;

  public void execute() {

    Dimensions dimensionless = Dimensions.Builder().build();
    Dimensions velocity = Dimensions.Builder().length(1).time(-1).build();
    Dimensions velocitySquared = Dimensions.Builder().length(2).time(-2).build();
    Dimensions diffusivity = Dimensions.Builder().length(2).time(-1).build();

    // Create mean(c)
    createMeanScalarMonitor("mNa_1+");

    // Create c * u_i vector field function
    createVectorFieldFunction("c_u_i", "c_u_i",
        "[$${Velocity}[0] * ${mNa_1+}, $${Velocity}[1] * ${mNa_1+}, $${Velocity}[2] * ${mNa_1+}]", velocity);

    // Create Velocity^2 field functions
    createVectorFieldFunction("Velocity ii", "vel_ii",
        "[$${Velocity}[0] * $${Velocity}[0], $${Velocity}[1] * $${Velocity}[1], $${Velocity}[2] * $${Velocity}[2]]",
        velocitySquared);
    createVectorFieldFunction("Velocity ij", "vel_ij",
        "[$${Velocity}[0] * $${Velocity}[1], $${Velocity}[0] * $${Velocity}[2], $${Velocity}[1] * $${Velocity}[2]]",
        velocitySquared);

    // Create Mean(Velocity^2) and Mean(Velocity) monitors
    for (int i = 0; i < 3; i++) {
      createMeanFlowMonitor("Velocity", i);
      createMeanFlowMonitor("vel_ii", i);
      createMeanFlowMonitor("vel_ij", i);

      // Create mean(c * u_i)
      createMeanFlowMonitor("c_u_i", i);
    }

    // Create Reynolds Stress field functions
    createVectorFieldFunction("Reynolds Stress ii", "rs_ii",
        "[${Meanvel_ii_0Monitor}-${MeanVelocity_0Monitor}*${MeanVelocity_0Monitor},${Meanvel_ii_1Monitor}-${MeanVelocity_1Monitor}*${MeanVelocity_1Monitor},${Meanvel_ii_2Monitor}-${MeanVelocity_2Monitor}*${MeanVelocity_2Monitor}]",
        velocitySquared);
    createVectorFieldFunction("Reynolds Stress ij", "rs_ij",
        "[${Meanvel_ij_0Monitor}-${MeanVelocity_0Monitor}*${MeanVelocity_1Monitor},${Meanvel_ij_1Monitor}-${MeanVelocity_0Monitor}*${MeanVelocity_2Monitor},${Meanvel_ij_2Monitor}-${MeanVelocity_1Monitor}*${MeanVelocity_2Monitor}]",
        velocitySquared);

    // Create mean(c' u_i')
    createVectorFieldFunction("Diffusion Stress", "R_c_u_i",
        "[${Meanc_u_i_0Monitor}-${MeanmNa_1+Monitor}*${MeanVelocity_0Monitor},${Meanc_u_i_1Monitor}-${MeanmNa_1+Monitor}*${MeanVelocity_1Monitor},${Meanc_u_i_2Monitor}-${MeanmNa_1+Monitor}*${MeanVelocity_2Monitor}]",
        velocity);

    // Create Reynolds Stress monitors
    for (int i = 0; i < 3; i++) {
      createMeanFlowMonitor("rs_ii", i);
      createMeanFlowMonitor("rs_ij", i);
    }

    // Output field functions

    // Upgraded
    createTensorFieldFunction("Reynolds Stress Tensor", "R_Tensor",
        "[ $${rs_ii}[0],$${rs_ij}[1],$${rs_ij}[2]; $${rs_ii}[1],$${rs_ij}[2]; $${rs_ii}[2] ]", velocitySquared);

    createScalarFieldFunction("Eddy Dissipation Rate", "epsilon",
        "${DynamicViscosity} *  ( pow(grad(sqrt($${rs_ii}[0]))[0], 2)+ pow(grad(sqrt($${rs_ii}[0]))[1], 2)+ pow(grad(sqrt($${rs_ii}[0]))[2], 2)+  pow(grad(sqrt($${rs_ii}[1]))[0], 2)+ pow(grad(sqrt($${rs_ii}[1]))[1], 2)+ pow(grad(sqrt($${rs_ii}[1]))[2], 2)+  pow(grad(sqrt($${rs_ii}[2]))[0], 2)+ pow(grad(sqrt($${rs_ii}[2]))[1], 2)+ pow(grad(sqrt($${rs_ii}[2]))[2], 2) )",
        Dimensions.Builder().length(2).time(-3).build());

    createScalarFieldFunction("Turbulent Kinetic Energy", "k", "0.5*mag($${rs_ii})", velocitySquared);

    createVectorFieldFunction("Concentration Gradient Vector", "GradC",
        "[ grad(${MeanmNa_1+Monitor})[0],grad(${MeanmNa_1+Monitor})[1],grad(${MeanmNa_1+Monitor})[2] ]",
        Dimensions.Builder().length(-1).build());

    createVectorFieldFunction("Relative Damkohler Number", "DaHat",
        "[ pow(1 + abs(${Meanc_u_i_0Monitor})/abs(${mNa_1+DiffCoef}*grad(${MeanmNa_1+Monitor})[0]),-1) , pow(1 + abs(${Meanc_u_i_1Monitor})/abs(${mNa_1+DiffCoef}*grad(${MeanmNa_1+Monitor})[1]),-1) , pow(1 + abs(${Meanc_u_i_2Monitor})/abs(${mNa_1+DiffCoef}*grad(${MeanmNa_1+Monitor})[2]),-1)  ] ",
        dimensionless);

    createScalarFieldFunction("Turbulent Schmidt Number GGDH", "Sc_t_2",
        "(0.2/(0.52)*${k}/(${epsilon})* (div(dotVector($$${R_Tensor}, $${GradC})) )) / (div($${R_c_u_i}))",
        dimensionless);
  }

  private void createScalarFieldFunction(String name, String identifier, String definition, Dimensions units) {
    Simulation simulation = getActiveSimulation();
    UserFieldFunction userFieldFunction = simulation.getFieldFunctionManager().createFieldFunction();
    userFieldFunction.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
    userFieldFunction.setPresentationName(name);
    userFieldFunction.setFunctionName(identifier);
    userFieldFunction.setDimensions(units);
    userFieldFunction.setDefinition(definition);
  }

  private void createVectorFieldFunction(String name, String identifier, String definition, Dimensions units) {
    Simulation simulation = getActiveSimulation();
    UserFieldFunction userFieldFunction = simulation.getFieldFunctionManager().createFieldFunction();
    userFieldFunction.getTypeOption().setSelected(FieldFunctionTypeOption.Type.VECTOR);
    userFieldFunction.setPresentationName(name);
    userFieldFunction.setFunctionName(identifier);
    userFieldFunction.setDimensions(units);
    userFieldFunction.setDefinition(definition);
  }

  private void createTensorFieldFunction(String Name, String Identifier, String Definition, Dimensions Units) {
    Simulation simulation = getActiveSimulation();
    UserFieldFunction userFieldFunction = simulation.getFieldFunctionManager().createFieldFunction();
    userFieldFunction.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SYMMETRIC_TENSOR);
    userFieldFunction.setPresentationName(Name);
    userFieldFunction.setFunctionName(Identifier);
    userFieldFunction.setDimensions(Units);
    userFieldFunction.setDefinition(Definition);
  }

  private void createMeanFlowMonitor(String vel, int component) {

    Simulation simulation = getActiveSimulation();

    FieldMeanMonitor fieldMeanMonitor = simulation.getMonitorManager().createMonitor(FieldMeanMonitor.class);
    fieldMeanMonitor.getParts().setQuery(null);
    Region region = simulation.getRegionManager().getRegion("Fluid");
    fieldMeanMonitor.getParts().setObjects(region);

    FieldFunction targetFieldFunction = ((FieldFunction) simulation.getFieldFunctionManager().getFunction(vel));
    VectorComponentFieldFunction vectorComponentFieldFunction = ((VectorComponentFieldFunction) targetFieldFunction
        .getComponentFunction(component));
    fieldMeanMonitor.setFieldFunction(vectorComponentFieldFunction);

    fieldMeanMonitor.setPresentationName("Mean " + vel + "_" + component);
  }

  private void createMeanScalarMonitor(String scalar) {

    Simulation simulation = getActiveSimulation();

    FieldMeanMonitor fieldMeanMonitor = simulation.getMonitorManager().createMonitor(FieldMeanMonitor.class);
    fieldMeanMonitor.getParts().setQuery(null);
    Region region = simulation.getRegionManager().getRegion("Fluid");
    fieldMeanMonitor.getParts().setObjects(region);

    FieldFunction targetFieldFunction = ((FieldFunction) simulation.getFieldFunctionManager().getFunction(scalar));
    fieldMeanMonitor.setFieldFunction(targetFieldFunction);

    fieldMeanMonitor.setPresentationName("Mean " + scalar);
  }

}
