package enigma.api.dto.response;

import enigma.sessions.model.ConfigEventView;
import enigma.sessions.model.HistoryView;
import enigma.sessions.model.MachineDefinition;
import enigma.sessions.model.ProcessRecordView;
import enigma.sessions.model.SessionView;
import enigma.shared.state.CodeState;
import enigma.shared.state.MachineState;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static MachineResponse machine(MachineDefinition machineDefinition) {
        return new MachineResponse(
                machineDefinition.machineName(),
                machineDefinition.xmlPath(),
                machineDefinition.loadedAt());
    }

    public static SessionResponse session(SessionView sessionView) {
        return new SessionResponse(
                sessionView.sessionId(),
                sessionView.machineName(),
                sessionView.status(),
                sessionView.openedAt(),
                sessionView.closedAt());
    }

    public static MachineStateResponse machineState(MachineState machineState) {
        return new MachineStateResponse(
                machineState.numOfRotors(),
                machineState.numOfReflectors(),
                machineState.stringsProcessed(),
                stateToText(machineState.ogCodeState()),
                stateToText(machineState.curCodeState())
        );
    }

    public static HistoryResponse history(HistoryView historyView) {
        return new HistoryResponse(
                historyView.scope(),
                historyView.sessionId(),
                historyView.machineName(),
                historyView.configurationEvents().stream().map(ResponseMapper::configEvent).toList(),
                historyView.processRecords().stream().map(ResponseMapper::processRecord).toList()
        );
    }

    private static ConfigEventResponse configEvent(ConfigEventView configEventView) {
        return new ConfigEventResponse(
                configEventView.id(),
                configEventView.sessionId(),
                configEventView.machineName(),
                configEventView.action(),
                configEventView.payload(),
                configEventView.createdAt()
        );
    }

    private static ProcessRecordResponse processRecord(ProcessRecordView processRecordView) {
        return new ProcessRecordResponse(
                processRecordView.id(),
                processRecordView.sessionId(),
                processRecordView.machineName(),
                processRecordView.code(),
                processRecordView.inputText(),
                processRecordView.outputText(),
                processRecordView.durationNanos(),
                processRecordView.processedAt()
        );
    }

    private static String stateToText(CodeState codeState) {
        if (codeState == null || codeState == CodeState.NOT_CONFIGURED) {
            return "<not configured>";
        }
        return codeState.toString();
    }
}
