package ai.serverapi.global.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public final class MessageVo {

    private final String message;
}
