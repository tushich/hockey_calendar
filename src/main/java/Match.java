import com.google.api.client.util.DateTime;

public class Match {
    public String tournament;
    public String round;
    public String number;
    public DateTime startDateTime;
    public String stadium;
    public String teams;
    public String count;
    public String protokolExist;
    public String linkMatch;
    public String matchID;

    String[] colNames = new String[]
            {       "Tournament",
                    "Round",
                    "Number",
                    "startDate",
                    "startTime",
                    "Stadium",
                    "teams",
                    "count",
                    "protokolExist",
                    "linkMatch",
                    "matchID"};

    public String compare(Match another_match)
    {
        String dif = "";
        if(tournament.equals(another_match.getTournament()))
        {
            dif.concat(String.format("\nИзменился турнир:\n%s -> \n%s", tournament, another_match.getTournament()));
        }
        if(round.equals(another_match.getRound()))
        {
            dif.concat(String.format("\nИзменился раунд:\n%s -> \n%s", round, another_match.getRound()));
        }
        if(round.equals(another_match.getRound()))
        {
            dif.concat(String.format("\nИзменился раунд:\n%s -> \n%s", round, another_match.getRound()));
        }



        return dif;
    }

    public boolean isEmpty()
    {
        return matchID.isEmpty();
    }
    public void setByColumnId(Integer colId, String value) {
        switch (colId)
        {
            case(0): setTournament(value);
            case(1): setRound(value);
            case(2): setNumber(value);
            //case(3): setStartDate(value); дату устанавлиаем через отдельный сеттер
            //case(4): setStartTime(value);
            case(5): setStadium(value);
            case(6): setTeams(value);
            case(7): setCount(value);
            case(8): setProtokolExist(value);
            case(9): setLinkMatch(value);
            case(10): setMatchID(value);

        }

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
        time.concat(":00");
        date = date.substring(9, 13) + "-" + date.substring(6, 8) + "-" + date.substring(3, 5);
        startDateTime = new DateTime(date + "T" + time + "+03:00");
        this.startDateTime = startDateTime;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }

    public void setProtokolExist(String protokolExist) {
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

    public DateTime getStartDateTime() {
        return startDateTime;
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

    public String getProtokolExist() {
        return protokolExist;
    }

    public String getLinkMatch() {
        return linkMatch;
    }

    public String getMatchID() {
        return matchID;
    }
}
