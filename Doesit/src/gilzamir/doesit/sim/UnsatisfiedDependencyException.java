package gilzamir.doesit.sim;


public class UnsatisfiedDependencyException extends RuntimeException {
    private Class dependency;
    private Class moduleType;
    
    public UnsatisfiedDependencyException(Class moduleType, Class dependency) {
        super(dependency.getSimpleName() + " is neccessary to " + moduleType.getSimpleName() + ", "
                + "but " + dependency.getSimpleName() + " was not found!");
        this.dependency = dependency;
        this.moduleType = moduleType;
    }

    public void setDependency(Class dependency) {
        this.dependency = dependency;
    }

    public void setModuleType(Class moduleType) {
        this.moduleType = moduleType;
    }

    public Class getModuleType() {
        return moduleType;
    }

    public Class getDependency() {
        return dependency;
    }
}
