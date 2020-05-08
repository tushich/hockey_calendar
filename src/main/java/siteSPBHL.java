import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public interface siteSPBHL {

    static TwoDimentionalArrayList<String> getTable(String url)
    {
        TwoDimentionalArrayList<String> tableCalender = new TwoDimentionalArrayList<>(); // TODO переделать на ассоциативный массив(hashmap вроде), чтобы обращаться по имени колонки и убрать первую строку с именами колонки
        Document doc;

        try
        {
            doc = Jsoup.connect(url).get();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.print("Ошибка чтения страницы HTML");
            return tableCalender;
        }

        Elements rows = doc.getElementById("MatchGridView").select("tr");

        for (int i = 0; i < rows.size(); i++)
        {

            Elements cols = rows.get(i).select((i == 0)? "th": "td");// разбиваем полученную строку по тегу  на столбы
            if(i == 0)
            {
                tableCalender.addToInnerArray(i, 7, "протокол");
                tableCalender.addToInnerArray(i, cols.size(), "linkMatch");
                tableCalender.addToInnerArray(i, cols.size() + 1, "matchID");
            }
            for (int j = 0; j < cols.size(); j++)
            {
                if(j == 5) // колонка с матчем
                {
                    Elements matchElements = cols.get(j).select("a");
                    if(matchElements.size() > 0)
                    {
                        // Вытащим matchID из ссылки. Состоит он из IDТурнира + v + IDМатча
                        String matchID = matchElements.get(0).attributes().toString();

                        // Добавим ссылку на матч
                        tableCalender.addToInnerArray(i, cols.size(), "https://spbhl.ru/" + matchID.replace(" href=\"", "")
                                                                                                          .replace("\"", "")
                                                                                                          .replace("&amp;", "&"));

                        matchID = matchID.replace(" href=\"Match.aspx?TournamentID=", "");
                        matchID = matchID.replace("MatchID=", "");
                        matchID = matchID.replace("\"", "");
                        matchID = matchID.replace("&amp;", "v");

                        tableCalender.addToInnerArray(i, cols.size() + 1, matchID);
                    }
                }
                if(j == 7)
                {
                    if(i != 0) tableCalender.addToInnerArray(i, j, (cols.get(j).select("a").size()) > 0? "Есть" : "Нет");
                }
                else
                {
                    tableCalender.addToInnerArray(i, j, cols.get(j).text());
                }
            }
        }
        return tableCalender;
    }
}
