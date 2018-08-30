package models.contributions;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("UNUSABLE")
public class UnusableContribution extends Contribution<UnusableContribution> {

	public enum Cause {
		NO_SPECIMEN(0, "unusable.cause.no.specimen"),
		TOO_MANY_SPECIMENS(1, "unusable.cause.too.many.specimens"),
		NO_LABEL(2, "unusable.cause.no.label"),
		TOO_MANY_LABELS(3, "unusable.cause.too.many.labels"),
		GROWNED_PLANT (4, "unusable.cause.growned.plant");
		
		private final Integer id;
		private final String i18nKey;
		
		Cause(Integer id, String i18nKey) {
			this.id = id;
			this.i18nKey = i18nKey;
		}

		public Integer getId() {
			return id;
		}

		public String getI18nKey() {
			return i18nKey;
		}
		
	}
	
	private Integer cause;

	@Override
	public boolean isInConflict(UnusableContribution other) {
		// TODO Auto-generated method stub
		
		return false;
	}

	@Override
	public void sanitize() {
		
	}

	@Override
	public void validate(UnusableContribution other) {
		// TODO Auto-generated method stub
		
	}

	public void setCause(Integer cause) {
		this.cause = cause;
	}

	public Integer getCause() {
		return cause;
	}
	
}
