package r01f.services.bootstrap.core.db.tx.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import r01f.services.core.persistence.CoreCRUDServicesForModelObjectBase;


public class CoreDBTransactionAdvisorHandlerDefault 
		implements CoreDBTransactionAdvisorHandler {
	
	@Override	
	public Advisor createFor(final TransactionInterceptor interceptor, 
	                         final Class<?>... basePackageClasses) {
	    
	    //[1] Pointcut 1 :  @Transacional based annotation   
	    Pointcut annotationPointcut = new AnnotationMatchingPointcut(null, jakarta.transaction.Transactional.class, true);

	    //[2] Pointcut 2 : Packages   
	    AspectJExpressionPointcut packagePointcut = new AspectJExpressionPointcut();        
	    StringBuilder expression = new StringBuilder();	    
	     // [2.1] Important! Set Fabric classes  [ look inside...a  jakarta @Transnsacion is used...]
	    Class<?>[] r01fFabricClasses = { CoreCRUDServicesForModelObjectBase.class };
	    
	     //[2.2] Build an array with  Fabric Classes +  Final Serices.
	    List<Class<?>> allAnchors = new ArrayList<>();
	    allAnchors.addAll(Arrays.asList(basePackageClasses));
	    allAnchors.addAll(Arrays.asList(r01fFabricClasses));
	    
	    for (int i = 0; i < allAnchors.size(); i++) {
	        expression.append("within(").append(allAnchors.get(i).getPackage().getName()).append("..*)");
	        if (i < allAnchors.size() - 1) expression.append(" || ");
	    }
	    packagePointcut.setExpression(expression.toString());

	    //[n] Join Poincuts
	    ComposablePointcut combinedPointcut = new ComposablePointcut((Pointcut) packagePointcut);
	    combinedPointcut.intersection(annotationPointcut); 

	    return new DefaultPointcutAdvisor(combinedPointcut, 
	    		                          interceptor);
	}
}
