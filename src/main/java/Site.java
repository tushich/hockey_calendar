import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface Site {
    // TODO Сделать в интерфейсе разные классы для работы с разными сайтами

    static List<Match> getMatchTable(String team_id, String siteID) {

        List<Match> matchTable = new ArrayList<>();
        if(siteID.equals("fhspb.ru") || siteID.equals("spbhl.ru"))
        {
            SiteSPBHL.getMatchTable(team_id, siteID, matchTable);
        }
        else
        {
            SiteRHL.getMatchTable(team_id, siteID, matchTable);
        }
        return matchTable;
        }
    }
    interface SiteSPBHL {
        static void getMatchTable(String team_id, String siteID, List<Match> matchTable)
        {
            int startDateCol = 2,
                    startTimeCol = 3,
                    protokolExistCol = 7,
                    tournamentCol = 0,
                    roundCol = 99,
                    numberCol = 1,
                    stadiumCol = 4,
                    teamsCol = 5,
                    countCol = 6;

            if(siteID.equals("fhspb.ru"))
            {
                startDateCol = 2;
                startTimeCol = 3;
                protokolExistCol = 7;
                tournamentCol = 0;
                roundCol = 99;
                numberCol = 1;
                stadiumCol = 4;
                teamsCol = 5;
                countCol = 6;

            }
            else //if(siteID.equals("spbhl.ru"))
            {
                startDateCol = 3;
                startTimeCol = 4;
                protokolExistCol = 8;
                tournamentCol = 0;
                roundCol = 1;
                numberCol = 2;
                stadiumCol = 5;
                teamsCol = 6;
                countCol = 7;
            }


            String url = String.format("https://" + siteID + "/Schedule?TeamID=%s", team_id);
            Document doc;

            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                //String errText = String.format("\nОшибка чтения страницы HTML %s\n Текст ошибки:%s", url, e.getMessage());
                //System.out.format(errText);
                //TelegramBot.getInstance().sendMsgToAdmin(errText);
                return;
            }

            Elements rows = new Elements();
            try {
                rows = doc.getElementById("MatchGridView").select("tr");
            }
            catch (Exception e) {
                return; // нет матчей
            }


            for (int i = 1; i < rows.size(); i++) {
                Match match = new Match();
                Elements cols = rows.get(i).select((i == 0) ? "th" : "td");// разбиваем полученную строку по тегу на столбы

                Elements matchElements = cols.get(teamsCol).select("a");

                if (matchElements.size() > 0) {
                    // Вытащим matchID из ссылки. Состоит он из IDТурнира + v + IDМатча
                    String matchID = matchElements.get(tournamentCol).attributes().toString();

                    // Добавим ссылку на матч
                    match.setLinkMatch("https://" + siteID + "/" + matchID.replace(" href=\"", "")
                            .replace("\"", "")
                            .replace("&amp;", "&"));

                    matchID = matchID.replace(" href=\"Match.aspx?TournamentID=", ""); // спбхл
                    matchID = matchID.replace(" href=\"Match?TournamentID=", ""); // фхспб
                    matchID = matchID.replace("MatchID=", "");
                    matchID = matchID.replace("\"", "");
                    matchID = matchID.replace("&amp;", "v");

                    match.setMatchID(matchID);
                }

                String time = cols.get(startTimeCol).text();
                time = time.replace("--:--","00:00");
                String date = cols.get(startDateCol).text();
                date = date.substring(9, 13) + "." + date.substring(6, 8) + "." + date.substring(3, 5);
                match.setStartDateTime(date, time);
                // TODO ПРотокол проверять по другому, сейчас посчитал что протокол это видеозапись матча ПримерСВидео:https://spbhl.ru/Match?TournamentID=5825&MatchID=90219
                match.setProtokolExist((cols.get(protokolExistCol).select("a[id=ReportHyperLink]").size()) > 0);
                match.setTournament(cols.get(tournamentCol).text());
                match.setNumber(cols.get(numberCol).text());
                match.setStadium(cols.get(stadiumCol).select("a[href]").attr("title"));
                match.setTeams(cols.get(teamsCol).text());
                match.setCount(cols.get(countCol).text());
                match.setTeam_id(team_id);
                match.setSiteID(siteID);

                matchTable.add(match);
        };


    }


    }
    interface SiteRHL{
        static void getMatchTable(String team_id, String siteID, List<Match> matchTable)
        {
            // // https://rhlspb.ru/%D0%9A%D0%BE%D0%BC%D0%B0%D0%BD%D0%B4%D0%B0/0f814f7cc4e7cb945ec935e76cbaa43c/%D0%A1%D0%B0%D0%BD%D0%BA%D1%82-%D0%9F%D0%B5%D1%82%D0%B5%D1%80%D0%B1%D1%83%D1%80%D0%B3/%D0%A1%D0%B5%D0%B7%D0%BE%D0%BD_2023-2024/4-%D0%B9_%D0%94%D0%B8%D0%B2%D0%B8%D0%B7%D0%B8%D0%BE%D0%BD/
            String url = String.format("https://rhlspb.ru/Команда/%s/", team_id);

            // получим самый верхний турнир и ссылку на него

            Document doc;

            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                String errText = String.format("\nОшибка чтения страницы команды HTML %s\n Текст ошибки:%s", url, e.getCause()); //getMessage());
                System.out.format(errText);
                TelegramBot.getInstance().sendMsgToAdmin(errText);
                return;
            }

            //url = doc.getElementsByClass("turnir").select("a[href]").get(0).attr("abs:href");
            for (Element turnir : doc.getElementsByClass("turnir").select("a[href]")) // турниров может быть мно, надо все обойти
            {
                url = turnir.attr("abs:href");

                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    String errText = String.format("\nОшибка чтения страницы турнира HTML %s\n Текст ошибки:%s", url, e.getMessage());
                    System.out.format(errText);
                    TelegramBot.getInstance().sendMsgToAdmin(errText);
                    return;
                }


                Elements rows = doc.getElementsByClass("calendar").select("div.item");

                for (Element row : rows) {
                    Match match = new Match();
                    Elements time = row.select("div.time");
                    Elements protokol = row.select("div.protokol").select("a[href]");
                    match.setLinkMatch(protokol.attr("abs:href"));
                    match.setMatchID(protokol.attr("href").replace("/%D0%98%D0%B3%D1%80%D0%B0/", "").replace("/", ""));
                    // 19-10-2023 в 22:15 формат "yyyy.MM.dd HH:mm"
                    String date = time.select("strong").text().substring(0,10);
                    date = date.substring(6,10) + "." + date.substring(3,5) + "." + date.substring(0,2);
                    match.setStartDateTime(date, time.select("strong").text().substring(13,18));

                    match.setTournament(time.get(0).childNode(3).childNode(0).toString());
                    match.setNumber("");

                    match.setStadium(time.get(0).childNode(2).toString().replace(", ", ""));
                    match.setTeams(row.select("div.team").get(0).text() + " - " + row.select("div.team").get(1).text());

                    String count = row.select("div.points").text();

                    match.setProtokolExist(!row.select("div.result").text().equals("")); // протокол добавляют сразу
                    match.setCount(count);
                    match.setTeam_id(team_id);
                    match.setSiteID(siteID);

                    matchTable.add(match);

                }

            }

        }

    }

