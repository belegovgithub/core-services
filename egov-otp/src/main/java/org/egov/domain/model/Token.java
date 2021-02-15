package org.egov.domain.model;

import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Getter
public class Token {
    @NotEmpty
    private final String tenantId;
    private String identity;
    @Setter
    private String number;
    private String uuid;
    private LocalDateTime expiryDateTime;
    @Setter
    private Long createdTime;
    private Long timeToLiveInSeconds;
    @Setter
    private boolean validated;
    private Date createdDate;

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiryDateTime);
    }

	@Override
	public String toString() {
		return "Token [tenantId=" + tenantId + ", identity=" + identity + ", number=" + number + ", uuid=" + uuid
				+ ", expiryDateTime=" + expiryDateTime + ", createdTime=" + createdTime + ", timeToLiveInSeconds="
				+ timeToLiveInSeconds + ", validated=" + validated + ", createdDate=" + createdDate + "]";
	}
}


