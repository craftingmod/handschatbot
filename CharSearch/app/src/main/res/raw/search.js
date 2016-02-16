function Search(name){
    document.getElementById("G_ctnMapleJobRankingSearch_g_txt_SearchWord").value = name;
    window.G_ctnMapleJobRankingSearch_g_txt_SearchWord.DoPostBack();
}
Search("%nick");