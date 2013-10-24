<%@ page import="org.amcworld.springcrm.Service" %>
<html>
<head>
  <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'service.label', default: 'Service')}" />
  <g:set var="entitiesName" value="${message(code: 'service.plural', default: 'Services')}" />
  <title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>

<body>
  <header>
    <h1><g:message code="${entitiesName}" /></h1>
    <g:render template="/layouts/toolbarForm" model="[formName: 'service']" />
  </header>
  <div id="content">
    <g:if test="${flash.message}">
    <div class="flash-message message" role="status">${raw(flash.message)}</div>
    </g:if>
    <g:if test="${serviceInstance.hasErrors() || serviceInstance?.pricing?.hasErrors()}">
    <div class="flash-message form-error-hint"><g:message code="default.form.errorHint" /></div>
    </g:if>
    <h2>${serviceInstance?.toString()}</h2>
    <g:form name="service-form" action="update" method="post"
      params="[returnUrl: params.returnUrl]">
      <g:hiddenField name="id" value="${serviceInstance?.id}" />
      <g:hiddenField name="version" value="${serviceInstance?.version}" />
      <g:render template="/service/form" />
    </g:form>
  </div>
</body>
</html>
