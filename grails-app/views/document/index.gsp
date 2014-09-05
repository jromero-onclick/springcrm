<html>
<head>
  <meta name="layout" content="main" />
  <title><g:message code="document.plural" default="Documents" /></title>
  <meta name="stylesheet" content="document" />
</head>

<body>
  <header>
    <h1><g:message code="document.plural" default="Documents" /></h1>
  </header>
  <form id="document-list-upload"
    data-url="${createLink(controller: 'document', action: 'upload')}"
    enctype="multipart/form-data">
    <aside id="action-bar">
      <h3><g:message code="default.actions" /></h3>
      <ul>
        <li>
          <g:button color="green" size="medium" class="create-folder-button"
            icon="plus" message="document.button.createFolder" />
        </li>
        <li>
          <g:button color="green" size="medium" class="fileinput-button"
            icon="plus">
            <span><g:message code="document.button.add" /></span>
            <input type="file" name="file" multiple="multiple" />
          </g:button>
        </li>
        <li>
          <g:button color="blue" size="medium" class="document-upload-start"
            icon="upload" message="document.button.upload" />
        </li>
        <li>
          <g:button color="orange" size="medium" class="document-upload-cancel"
            icon="ban" message="document.button.cancel" />
        </li>
      </ul>
    </aside>
    <div id="content">
      <g:if test="${flash.message}">
      <div class="flash-message message" role="status">${raw(flash.message)}</div>
      </g:if>
      <h2><g:message code="document.allDocuments.label" default="All documents" /></h2>
      <div id="document-list" class="document-list lg" data-list="document"
        data-list-url="${createLink(controller: 'document', action: 'dir')}"
        data-download-url="${createLink(controller: 'document', action: 'download')}"
        data-create-folder-url="${createLink(controller: 'document', action: 'createFolder')}"
        ></div>
      <div class="document-upload-filelist">
        <h3><g:message code="document.upload.header" /></h3>
        <input type="hidden" id="current-path" name="path" value="" />
        <table class="table table-striped" role="presentation">
          <tbody></tbody>
        </table>
      </div>
      <div id="create-folder-dialog"
        title="${message(code: 'document.createFolder.title')}"
        style="display: none;">
        <p><g:message code="document.createFolder.description" /></p>
        <input type="text" id="create-folder-name" size="40"
          placeholder="${message(code: 'document.createFolder.placeholder')}" />
       </div>
    </div>
  </form>
  <script id="add-upload-request-template" type="text/x-tmpl-mustache">
    {{#files}}
    <tr class="upload-request-template">
      <td class="preview"></td>
      <td class="name">{{name}}</td>
      <td class="size"></td>
      <td>
        <div class="progress" role="progressbar" aria-valuemin="0"
          aria-valuemax="100" aria-valuenow="0">
          <div class="progress-bar progress-bar-success"
            style="width: 0%;"></div>
        </div>
      </td>
      <td>
        <button class="button green document-upload-start" disabled="disabled">
          <i class="fa fa-upload"></i> <g:message code="document.button.start" />
        </button>
        <button class="button orange document-upload-cancel">
          <i class="fa fa-ban"></i> <g:message code="default.btn.cancel" />
        </button>
      </td>
    </tr>
    {{/files}}
  </script>
  <content tag="scripts">
    <asset:javascript src="document" />
  </content>
</body>
</html>
