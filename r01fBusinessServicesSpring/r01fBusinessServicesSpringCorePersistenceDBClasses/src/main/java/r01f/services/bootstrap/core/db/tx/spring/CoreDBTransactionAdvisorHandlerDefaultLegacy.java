package r01f.services.bootstrap.core.db.tx.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import r01f.services.core.persistence.CoreCRUDServicesForModelObjectBase;

public class CoreDBTransactionAdvisorHandlerDefaultLegacy 
		implements CoreDBTransactionAdvisorHandler {
	
	@Override	
	public Advisor createFor(final TransactionInterceptor interceptor, 
	                         final Class<?>... basePackageClasses) {
	    //[1] Important! Set Fabric classes  [ look inside...a  jakarta @Transnsacion is used...]
	    Class<?>[] r01fFabricClasses = { CoreCRUDServicesForModelObjectBase.class };
	    
	    //[2] Build an array with  Fabric Classes +  Final Serices.
	    List<Class<?>> allAnchors = new ArrayList<>();
	    allAnchors.addAll(Arrays.asList(basePackageClasses));
	    allAnchors.addAll(Arrays.asList(r01fFabricClasses));
	    
	    // [3] Build  AOP PointCut and expression
	    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();        
	    StringBuilder expression = new StringBuilder();
	    for (int i = 0; i < allAnchors.size(); i++) {
	        String packageName = allAnchors.get(i).getPackage().getName();
	        // aop scope expresion ,pe within(dena.core.person..*)
	        expression.append("within(")
	        		  .append(packageName).append("..*)");	        
	        if (i < allAnchors.size() - 1) {
	            expression.append(" || ");
	        }
	    }
	    pointcut.setExpression(expression.toString());
	    return new DefaultPointcutAdvisor(pointcut, interceptor);
	}
}
