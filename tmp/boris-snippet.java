final LanguageInfo language = env.getLanguages().get("js");
final Class<? extends Tag> writeVarExpTag = Tag.findProvidedTag(language, "WriteVariableExpression");
final Class<? extends Tag> writePropExpTag = Tag.findProvidedTag(language, "WritePropertyExpression");
final SourceFilter sourceFilter = SourceFilter.newBuilder().includeInternal(false).build();
final SourceSectionFilter sourceSectionFilter = SourceSectionFilter.newBuilder().tagIs(writeVarExpTag, writePropExpTag).includeInternal(false).sourceFilter(sourceFilter).build();

Runtime.getRuntime().addShutdownHook(new Thread(() -> {
  for (Map.Entry<SourceSection, Set<String>> entry : data.entrySet()) {
    System.out.print(entry.getKey());
    System.out.print(":");
    for (String s : entry.getValue()) {
      System.out.print(s);
      System.out.print(",");
    }
    System.out.println("");
  }
}));

final EventBinding<ExecutionEventNodeFactory> binding = env.getInstrumenter().attachExecutionEventFactory(sourceSectionFilter, new ExecutionEventNodeFactory() {
  @Override
  public ExecutionEventNode create(EventContext context) {
    return new ExecutionEventNode() {
      @Override
      protected void onReturnValue(VirtualFrame frame, Object result) {
        doreturn(result);
      }

      @TruffleBoundary
      private void doreturn(Object result) {
        final SourceSection sourceSection = context.getInstrumentedSourceSection();
        if (result != null && sourceSection.getCharLength() < 100) {
          final String string = (result instanceof String || result instanceof Integer || result instanceof Double || result instanceof Boolean) ? result.toString() : env.toString(env.findLanguage(result), result);
          final Set<String> stringList = data.computeIfAbsent(sourceSection, (SourceSection ss) -> new HashSet<>());
          stringList.add(string);
        }
      }
    };
  }
});
}

Map<SourceSection, Set<String>> data = new HashMap<>();
