import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InfractionChicago {

    // issue_date;license_plate_number;violation_code;unit_description;fine_level1_amount;community_area_name
    // 2005-03-10
    // 16:30:00;25515fcf196d56759b5ebdf9242553b7123f5233184cfb38c2a3f45cc33fe5c8;0964080A;CPD;50;LOOP

    private Date infractionDate;
    private String licensePlateNumber;
    private String violationCode; // reference to the infractionsCHI.csv
    private String unitDescription;
    private String communityAreaName;
}
