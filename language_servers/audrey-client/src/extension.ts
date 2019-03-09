// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient';
import * as child_process from 'child_process';

let client: LanguageClient;


async function makeAudreyProcess(): Promise<child_process.ChildProcess> {
	return child_process.spawn("audrey-ls");
};

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Audrey client initialized.');

	let clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: 'file', language: 'javascript' }, { scheme: 'file', language: 'ruby' }],
	};

	const serverOptions: ServerOptions = async () => {
		return makeAudreyProcess();
	};

	client = new LanguageClient(
		'audrey-client',
		'Audrey LSP client',
		serverOptions,
		clientOptions
	);

	client.start();
}

// this method is called when your extension is deactivated
export function deactivate() {
	if (!client) {
		return undefined;
	}

	return client.stop();
}
