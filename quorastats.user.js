// ==UserScript==
// @name          Quora Statistics
// @namespace     http://www.ankurdave.com
// @description	  Scrapes Quora and calculates statistics.
// @include       http://www.quora.com/*
// ==/UserScript==
(function(tab) {
    $(".feed_item .rating").click(function() { alert("hi"); });
    console.log($(".feed_item").map(function(index, element) {
        return {
            permalink: $(this).children(".answer_permalink").attr("href"),
            votes: $(this).children(".voter_count").html()
        };
    }));
})();