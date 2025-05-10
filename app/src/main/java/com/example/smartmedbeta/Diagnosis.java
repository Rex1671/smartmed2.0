package com.example.smartmedbeta;
public class Diagnosis {
    private String condition;
    private String certainty;
    private String severity;
    private String generalAdvice;
    private String actionRequired;



    public Diagnosis() {}

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getCertainty() { return certainty; }
    public void setCertainty(String certainty) { this.certainty = certainty; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getGeneralAdvice() { return generalAdvice; }
    public void setGeneralAdvice(String generalAdvice) { this.generalAdvice = generalAdvice; }

    public String getActionRequired() { return actionRequired; }
    public void setActionRequired(String actionRequired) { this.actionRequired = actionRequired; }
}
