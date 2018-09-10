<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>
<html>
	<body>
		<h:form>
			<div style="border:1px solid">
				JSF ClassLoader: <h:outputText value="#{bean.jsfClassLoader}"/><br/><br/>
				Name: <h:inputText value="#{bean.name}"/><br/><br/>
				<h:commandButton value="Hello" action="#{bean.hello}"/><br/>
			</div>
			<br/>
			Response: <h:outputText value="#{bean.response}"/>
		</h:form>
	</body>
</html>
</f:view>
