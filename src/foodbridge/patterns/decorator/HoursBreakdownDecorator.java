package foodbridge.patterns.decorator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import foodbridge.models.operations.VolunteerHours;

/**
 * DECORATOR - ConcreteDecorator. Adds total hours per source.
 */
public class HoursBreakdownDecorator extends CertificateDecorator {
    private final List<VolunteerHours> hoursEntries;

    public HoursBreakdownDecorator(CertificateComponent wrapped, List<VolunteerHours> hoursEntries) {
        super(wrapped);
        this.hoursEntries = hoursEntries;
    }

    @Override
    public String generate() {
        Map<String, Double> totals = hoursEntries.stream().collect(Collectors.groupingBy(
                VolunteerHours::getSource, TreeMap::new, Collectors.summingDouble(VolunteerHours::getHours)));
        StringBuilder text = new StringBuilder(super.generate()).append("\n\nHours breakdown:");
        totals.forEach((source, hours) -> text.append(String.format("%n  %s: %.2f", source, hours)));
        return text.toString();
    }
}
