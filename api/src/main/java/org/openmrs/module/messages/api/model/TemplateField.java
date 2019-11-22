package org.openmrs.module.messages.api.model;

import org.openmrs.Concept;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "messages.TemplateField")
@Table(name = "messages_template_field")
public class TemplateField extends AbstractBaseOpenmrsData {
    
    private static final long serialVersionUID = -5617673897191245574L;
    
    @Id
    @GeneratedValue
    @Column(name = "messages_template_field_id")
    private Integer id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "mandatory", nullable = false)
    private Boolean mandatory;
    
    @Column(name = "default_value", columnDefinition = "text")
    private String defaultValue;
    
    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @OneToOne
    @JoinColumn(name = "value_concept")
    private Concept valueConcept;
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getMandatory() {
        return mandatory;
    }
    
    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public Template getTemplate() {
        return template;
    }
    
    public void setTemplate(Template template) {
        this.template = template;
    }
    
    public Concept getValueConcept() {
        return valueConcept;
    }
    
    public void setValueConcept(Concept valueConcept) {
        this.valueConcept = valueConcept;
    }
}
