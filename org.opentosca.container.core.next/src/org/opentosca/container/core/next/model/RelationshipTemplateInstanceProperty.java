package org.opentosca.container.core.next.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(RelationshipTemplateInstanceProperty.TABLE_NAME)
public class RelationshipTemplateInstanceProperty extends Property {

  private static final long serialVersionUID = -8847410322957873980L;

  public static final String TABLE_NAME =
      RelationshipTemplateInstance.TABLE_NAME + "_" + Property.TABLE_NAME;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RELATIONSHIP_TEMPLATE_INSTANCE_ID")
  private RelationshipTemplateInstance relationshipTemplateInstance;


  public RelationshipTemplateInstanceProperty() {
    super();
  }

  public RelationshipTemplateInstanceProperty(final String name, final String value) {
    super(name, value, null);
  }

  public RelationshipTemplateInstance getRelationshipTemplateInstance() {
    return this.relationshipTemplateInstance;
  }

  public void setRelationshipTemplateInstance(
      final RelationshipTemplateInstance relationshipTemplateInstance) {
    this.relationshipTemplateInstance = relationshipTemplateInstance;
    if (!relationshipTemplateInstance.getProperties().contains(this)) {
      relationshipTemplateInstance.getProperties().add(this);
    }
  }
}
