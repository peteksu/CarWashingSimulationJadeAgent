public class CarEntity {

	String entityName;
	String entityType; 
	int entityOrder; ; 
	int creationTime;
	
	public CarEntity(String entityName, String entityType, int order, int creationTime) {
		super();
		this.entityName = entityName;
		this.entityType = entityType;
		this.entityOrder = order; 
		this.creationTime = creationTime;
	} 
	
	
	
}
