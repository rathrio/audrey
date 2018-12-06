import {
  createConnection,
  TextDocuments,
  TextDocument,
  Diagnostic,
  DiagnosticSeverity,
  ProposedFeatures,
  InitializeParams,
  DidChangeConfigurationNotification,
  CompletionItem,
  CompletionItemKind,
  TextDocumentPositionParams
} from "vscode-languageserver";

import * as Redis from "ioredis";
const redis = new Redis();

interface Sample {
  identifier: string;
  metaObject: string;
  value: string;
  rootNodeId: string;
  category: "ARGUMENT" | "RETURN";
  source: string;
  sourceLine: number;
  sourceIndex: number;
  sourceLength: number;
  sourceCharacters: string;
}

let samples: Sample[];

redis.smembers("audrey_samples").then((json_samples: string[]) => {
  samples = json_samples.map(json => JSON.parse(json));

  samples.forEach(sample => {
    console.log(sample);
  });
});

// // Create a connection for the server. The connection uses Node's IPC as a transport.
// // Also include all preview / proposed LSP features.
// let connection = createConnection(ProposedFeatures.all);

// // Create a simple text document manager. The text document manager
// // supports full document sync only
// let documents: TextDocuments = new TextDocuments();

// let hasConfigurationCapability: boolean = false;
// let hasWorkspaceFolderCapability: boolean = false;
// let hasDiagnosticRelatedInformationCapability: boolean = false;

// connection.onInitialize((params: InitializeParams) => {
// 	let capabilities = params.capabilities;

// 	// Does the client support the `workspace/configuration` request?
// 	// If not, we will fall back using global settings
// 	hasConfigurationCapability = !!(capabilities.workspace && !!capabilities.workspace.configuration);
// 	hasWorkspaceFolderCapability = !!(capabilities.workspace && !!capabilities.workspace.workspaceFolders);
// 	hasDiagnosticRelatedInformationCapability =
// 		!!(capabilities.textDocument &&
// 		capabilities.textDocument.publishDiagnostics &&
// 		capabilities.textDocument.publishDiagnostics.relatedInformation);

// 	return {
// 		capabilities: {
// 			textDocumentSync: documents.syncKind,
// 			// Tell the client that the server supports code completion
// 			// completionProvider: {
// 			// 	resolveProvider: true
// 			// }
// 		}
// 	};
// });

// connection.onInitialized(() => {
// 	if (hasConfigurationCapability) {
// 		// Register for all configuration changes.
// 		connection.client.register(
// 			DidChangeConfigurationNotification.type,
// 			undefined
// 		);
// 	}
// 	if (hasWorkspaceFolderCapability) {
// 		connection.workspace.onDidChangeWorkspaceFolders(_event => {
// 			connection.console.log('Workspace folder change event received.');
// 		});
// 	}
// });

// connection.onDidOpenTextDocument((params) => {
// 	// A text document got opened in VSCode.
// 	// params.uri uniquely identifies the document. For documents store on disk this is a file URI.
// 	// params.text the initial full content of the document.
// 	connection.console.log(`${params.textDocument.uri} opened.`);
// });

// // connection.onHover((params) => {
// //   console.log("GOT HERE");
// // });

// // Make the text document manager listen on the connection
// // for open, change and close text document events
// documents.listen(connection);

// // Listen on the connection
// connection.listen();
