import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Match {
    public String tournament;

    public String team_id;
    public String round;
    public String number;
    public Date startDateTime;
    public String stadium;
    public String teams;
    public String count;
    public Boolean protokolExist;
    public String linkMatch;
    public String matchID;

    public String compare(Match new_match)
    {
        String dif = "";
        if(!tournament.equals(new_match.getTournament()))
        {
            dif = dif.concat(String.format("\nИзменился турнир:\n%s -> \n%s", tournament, new_match.getTournament()));
        }
        if(!round.equals(new_match.getRound()))
        {
            dif = dif.concat(String.format("\nИзменился раунд:\n%s -> \n%s", round, new_match.getRound()));
        }
        if(!startDateTime.equals(new_match.getStartDateTime()))
        {
            dif = dif.concat(String.format("\nИзменилась дата и время:\n%s -> \n%s", getDateString(startDateTime), getDateString(new_match.getStartDateTime())));
        }
        if(!stadium.equals(new_match.getStadium()))
        {
            dif = dif.concat(String.format("\nИзменился стадион:\n%s -> \n%s", stadium, new_match.getStadium()));
        }
        if(!protokolExist.equals(new_match.getProtokolExist()))
        {
            dif = dif.concat("\nДобавлен протокол");
        }
        if(!count.equals(new_match.getCount()))
        {
            dif = dif.concat("\nОбновлен счет: " + new_match.getCount());
        }

        return dif;
    }

    public boolean isEmpty()
    {
        return matchID==null || matchID.isEmpty();
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setTournament(String tournament) {
        this.tournament = tournament;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public void setStartDateTime(String date, String time) {
        date = date.substring(9, 13) + "." + date.substring(6, 8) + "." + date.substring(3, 5);
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ENGLISH);
        try {
            this.startDateTime  = format.parse(date + " " + time);
        } catch (ParseException e) {
            System.out.format("\nОшибка установки даты начала для Матча %s\nТекст ошибки:%s", this.matchID, e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void setStartDateTime(Date dateTime) {
        this.startDateTime = dateTime;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }

    public void setProtokolExist(boolean protokolExist) {
        this.protokolExist = protokolExist;
    }

    public void setLinkMatch(String linkMatch) {
        this.linkMatch = linkMatch;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public String getTournament() {
        return tournament;
    }

    public String getRound() {
        return round;
    }

    public String getNumber() {
        return number;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public String getStringStartDateTime() {
        return getDateString(startDateTime);
    }

    public String getStadium() {
        return stadium;
    }

    public String getTeams() {
        return teams;
    }

    public String getCount() {
        return count;
    }

    public Boolean getProtokolExist() {
        return protokolExist;
    }

    public String getLinkMatch() {
        return linkMatch;
    }

    public String getMatchID() {
        return matchID;
    }

    private String getDateString(Date dt)
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return df.format(dt);
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getTeam_id() {
        return team_id;
    }
}
