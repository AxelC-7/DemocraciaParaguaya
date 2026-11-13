package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class Trayectoria {
    private String personaId;
    private List<FormacionAcademica> formacionAcademica = new ArrayList<>();
    private List<ExperienciaProfesional> experienciaProfesional = new ArrayList<>();

    public Trayectoria() {}
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }
    public List<FormacionAcademica> getFormacionAcademica() { return formacionAcademica; }
    public void setFormacionAcademica(List<FormacionAcademica> formacionAcademica) { this.formacionAcademica = formacionAcademica; }
    public List<ExperienciaProfesional> getExperienciaProfesional() { return experienciaProfesional; }
    public void setExperienciaProfesional(List<ExperienciaProfesional> experienciaProfesional) { this.experienciaProfesional = experienciaProfesional; }
}
