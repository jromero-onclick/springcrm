<%@ page import="org.amcworld.springcrm.Product" %>
<html>
<head>
  <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
  <g:set var="entitiesName" value="${message(code: 'product.plural', default: 'Products')}" />
  <title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>

<body>
  <header>
    <h1><g:message code="${entitiesName}" /></h1>
    <g:render template="/layouts/toolbarForm" model="[formName: 'product']" />
  </header>
  <div id="content">
    <g:if test="${flash.message}">
    <div class="flash-message message" role="status">${raw(flash.message)}</div>
    </g:if>
    <g:hasErrors bean="${productInstance}">
    <div class="flash-message form-error-hint"><g:message code="default.form.errorHint" /></div>
    </g:hasErrors>
    <h2>${productInstance?.toString()}</h2>
    <g:form name="product-form" action="update" method="post"
      params="[returnUrl:params.returnUrl]">
      <g:hiddenField name="id" value="${productInstance?.id}" />
      <g:hiddenField name="version" value="${productInstance?.version}" />
      <g:render template="/product/form" />
    </g:form>
  </div>
</body>
</html>
