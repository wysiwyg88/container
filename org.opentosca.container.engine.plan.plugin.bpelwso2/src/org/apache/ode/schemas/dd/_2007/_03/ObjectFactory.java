//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.16 at 05:29:02 PM MESZ 
//


package org.apache.ode.schemas.dd._2007._03;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apache.ode.schemas.dd._2007._03 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Deploy_QNAME = new QName("http://www.apache.org/ode/schemas/dd/2007/03", "deploy");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.ode.schemas.dd._2007._03
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TService }
     * 
     */
    public TService createTService() {
        return new TService();
    }

    /**
     * Create an instance of {@link TInvoke.Binding }
     * 
     */
    public TInvoke.Binding createTInvokeBinding() {
        return new TInvoke.Binding();
    }

    /**
     * Create an instance of {@link TCleanup }
     * 
     */
    public TCleanup createTCleanup() {
        return new TCleanup();
    }

    /**
     * Create an instance of {@link TInvoke }
     * 
     */
    public TInvoke createTInvoke() {
        return new TInvoke();
    }

    /**
     * Create an instance of {@link TDeployment.Process }
     * 
     */
    public TDeployment.Process createTDeploymentProcess() {
        return new TDeployment.Process();
    }

    /**
     * Create an instance of {@link TDeployment }
     * 
     */
    public TDeployment createTDeployment() {
        return new TDeployment();
    }

    /**
     * Create an instance of {@link TMexInterceptor }
     * 
     */
    public TMexInterceptor createTMexInterceptor() {
        return new TMexInterceptor();
    }

    /**
     * Create an instance of {@link TSchedule }
     * 
     */
    public TSchedule createTSchedule() {
        return new TSchedule();
    }

    /**
     * Create an instance of {@link TDeployment.Process.MexInterceptors }
     * 
     */
    public TDeployment.Process.MexInterceptors createTDeploymentProcessMexInterceptors() {
        return new TDeployment.Process.MexInterceptors();
    }

    /**
     * Create an instance of {@link TEnableSharing }
     * 
     */
    public TEnableSharing createTEnableSharing() {
        return new TEnableSharing();
    }

    /**
     * Create an instance of {@link TScopeEvents }
     * 
     */
    public TScopeEvents createTScopeEvents() {
        return new TScopeEvents();
    }

    /**
     * Create an instance of {@link TProcessEvents }
     * 
     */
    public TProcessEvents createTProcessEvents() {
        return new TProcessEvents();
    }

    /**
     * Create an instance of {@link TDeployment.Process.Property }
     * 
     */
    public TDeployment.Process.Property createTDeploymentProcessProperty() {
        return new TDeployment.Process.Property();
    }

    /**
     * Create an instance of {@link TEnableEventList }
     * 
     */
    public TEnableEventList createTEnableEventList() {
        return new TEnableEventList();
    }

    /**
     * Create an instance of {@link TProvide }
     * 
     */
    public TProvide createTProvide() {
        return new TProvide();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TDeployment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.apache.org/ode/schemas/dd/2007/03", name = "deploy")
    public JAXBElement<TDeployment> createDeploy(TDeployment value) {
        return new JAXBElement<TDeployment>(_Deploy_QNAME, TDeployment.class, null, value);
    }

}
