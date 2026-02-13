package enigma.api.dto.response;

import enigma.sessions.model.ConfigEventView;
import enigma.sessions.model.HistoryView;
import enigma.sessions.model.ProcessOutcome;
import enigma.sessions.model.ProcessRecordView;
import enigma.shared.state.CodeState;
import enigma.shared.state.MachineState;
import enigma.shared.utils.CodeStateCompactFormatter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ApiContractMapper {

    private static final String NOT_CONFIGURED = "<not configured>";

    private ApiContractMapper() {
    }

    public static ConfigStatusApiResponse configStatus(MachineState state, boolean verbose) {
        EnigmaCodeStructureResponse originalCode = verbose ? codeStructure(state.ogCodeState()) : null;
        EnigmaCodeStructureResponse currentCode = verbose ? codeStructure(state.curCodeState()) : null;

        return new ConfigStatusApiResponse(
                state.numOfRotors(),
                state.numOfReflectors(),
                state.stringsProcessed(),
                originalCode,
                currentCode,
                CodeStateCompactFormatter.originalCodeCompact(state.ogCodeState()),
                CodeStateCompactFormatter.currentRotorsPositionCompact(state.curCodeState())
        );
    }

    public static ProcessApiResponse process(ProcessOutcome outcome) {
        return new ProcessApiResponse(
                outcome.output(),
                CodeStateCompactFormatter.currentRotorsPositionCompact(outcome.machineState().curCodeState())
        );
    }

    public static Map<String, List<HistoryEntryResponse>> history(HistoryView historyView) {
        List<ConfigEventView> configurations = historyView.configurationEvents();
        List<ProcessRecordView> records = historyView.processRecords();

        Map<String, List<HistoryEntryResponse>> grouped = new LinkedHashMap<>();
        int configIndex = 0;
        ConfigEventView activeConfig = null;

        for (ProcessRecordView record : records) {
            while (configIndex < configurations.size()
                    && !configurations.get(configIndex).createdAt().isAfter(record.processedAt())) {
                activeConfig = configurations.get(configIndex);
                configIndex++;
            }

            String key = activeConfig == null ? NOT_CONFIGURED : configDescription(activeConfig);
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(new HistoryEntryResponse(
                            record.inputText(),
                            record.outputText(),
                            TimeUnit.NANOSECONDS.toMillis(record.durationNanos())));
        }

        return grouped;
    }

    private static EnigmaCodeStructureResponse codeStructure(CodeState codeState) {
        if (codeState == null || codeState == CodeState.NOT_CONFIGURED) {
            return new EnigmaCodeStructureResponse(List.of(), "", List.of());
        }

        int size = Math.min(codeState.rotorIds().size(),
                Math.min(codeState.positions().length(), codeState.notchDist().size()));
        List<RotorSelectionWithNotchResponse> rotors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rotors.add(new RotorSelectionWithNotchResponse(
                    codeState.rotorIds().get(i),
                    String.valueOf(codeState.positions().charAt(i)),
                    codeState.notchDist().get(i)
            ));
        }

        List<PlugConnectionResponse> plugs = new ArrayList<>();
        String plugStr = codeState.plugStr();
        if (plugStr != null) {
            for (int i = 0; i + 1 < plugStr.length(); i += 2) {
                plugs.add(new PlugConnectionResponse(
                        String.valueOf(plugStr.charAt(i)),
                        String.valueOf(plugStr.charAt(i + 1))
                ));
            }
        }

        return new EnigmaCodeStructureResponse(rotors, codeState.reflectorId(), plugs);
    }

    private static String configDescription(ConfigEventView configEvent) {
        String payload = configEvent.payload();
        if (payload == null || payload.isBlank()) {
            return configEvent.action();
        }
        return payload;
    }
}
