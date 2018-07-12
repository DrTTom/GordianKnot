var network;
var selectedNode = "";
var zoomEnabled = true;
var urlPrefix = (window.location.protocol == "file:" ?
                 "http://localhost:4567/": "");

get(urlPrefix+"view/name", function (name) {
   document.getElementById("projectName").innerHTML = name;
});
get(urlPrefix+"view", showGraph);
get(urlPrefix+"view/classpath", showPath);
get(urlPrefix+"view/unrefReport", showReport);
get(urlPrefix+"view/missingReport", showMissingReport);
get(urlPrefix+"view/metrics", showMetrics);

function showMetrics(value)
{
    var response = JSON.parse(value);
    setNewContent("metricsTmpl", response, "metrics");
}

function setNewContent(templateId, data, targetId) {
   insertContent(resolve(templateId, data), targetId);
}

function insertContent(content, targetId) {
   const target = document.getElementById(targetId);
   target.innerHTML =content;
}

function resolve(tmplName, value) {
   const tplInfo = _.template(document.getElementById(tmplName).innerHTML);
   return tplInfo(value);
}

function get(url, callBack) {
   var req = new XMLHttpRequest();
   req.open("GET", url, true);
   req.send();
   req.onreadystatechange = function (e) {
      if (req.readyState == 4) {
         callBack(req.responseText);
      }
   };
   req.addEventListener("error", function () { alert("Server not responding."); } );
}

function showPath(value) {
   var response = JSON.parse(value);
   setNewContent("classpathTmpl", {entries: response}, "classpath");
}

function selectNodesByName(nodeName) {
   get(urlPrefix+"view/nodesByName/"+nodeName, function (value) {
      var ids = JSON.parse(value);
      selectNodesById(ids);
      openPage("graph", document.getElementById("defaultOpen"));
      window.scrollTo(0, 0);
   });
}

function selectNodesById(ids) {
   network.selectNodes(ids);
   if (ids.length == 1) {
      getNodeInfo(ids[0]);
   }
}

function showReport(value) {
   var report = JSON.parse(value);
   setNewContent("refReportTmpl", report, "refReport");
}

function showMissingReport(value) {
   var report = JSON.parse(value);
   setNewContent("missingReportTmpl", report, "missingReport");
}

function stopMovingNodes(params) {
   network.setOptions({
      physics: false
   });
   zoomEnabled = true;
}

function showGraph(responseText) {
   showParsedGraph(JSON.parse(responseText));
}


function showParsedGraph(response) {

   var nodes = new vis.DataSet(response.nodes);
   var edges = new vis.DataSet(response.edges);

   nodes.forEach( (node) => {
      node.y = node.level*10;
      nodes.update(node);
   } );
   
   var container = document.getElementById("mynetwork");
   var data = {
      nodes: nodes,
      edges: edges
   };
   var gray = "#555555";
   function createGroupProps(bgNormal, bgSelected) {
      return {
         color: {
            background: bgNormal,
            border: gray,
            hover: {
               background: bgSelected,
               border: "black"
            },
            highlight: {
               background: bgSelected,
               border: "black"
            }
         }
      };
   }

   var options = {
      physics: false,
      interaction: {
         hover: true
      },
      groups: {
         "jar": createGroupProps("#e7a5ab","#ebb5ba"),
         "dir": createGroupProps("#afd6e7","#dbecf4"),
         "package": createGroupProps("#aec489","#d2debe"),
         "class": createGroupProps("#ffed9e","#fff5ca")
      },
      layout:{randomSeed:2}
   };
   network = new vis.Network(container, data, options);
   selectedNode = "";
   insertContent("Click on node or arc to select it.", "description");
   zoomEnabled = false;
   distribute();
   network.on("stabilized", stopMovingNodes);

   network.on("initRedraw", function (params) {
      if (!zoomEnabled) {
         network.fit();
      }
   });
   network.on("click", function (params) {
      selectedNode = "";
      params.event = "[original event]";
      if (params.nodes.length == 1) {
         getNodeInfo(params.nodes[0]);
      } else if (params.edges.length == 1) {
         getArcInfo(params.edges[0]);
      } else {
         insertContent("nothing selected", "description");
      }
   });
   updateFilterList();
   return response;
}

function distribute() {
   network.setOptions({
      physics: true
   });
}

function getNodeInfo(id) {
   get(urlPrefix+"view/node/" + id, function (e) {
      var nodeInfo = JSON.parse(e);
      nodeInfo.id = id;
      selectedNode = id;
      setNewContent("nodeInfo", nodeInfo, "description");
      Array.from(description.querySelectorAll("button")).forEach((button) => {
         button.addEventListener("click", function (evt) {
            console.log(evt.target);
            const mode = evt.target.getAttribute("change-list-mode");
            changeListMode(mode);
         });
      });
   });
}

function changeListMode(mode) {
   if (selectedNode != "") {
      get(urlPrefix+"view/node/" + selectedNode + "/listmode/" + mode, (resp) => {
         var result = JSON.parse(resp);
         showParsedGraph(result.first);
         selectNodesById(result.second);
      });
   }
}

function setFilter(value) {
   get(urlPrefix+"view/filters/" + value, showGraph);
}

function impliedOnly(value) {
   if (selectedNode!="") {
      get(urlPrefix+"view/filters/impliedBy/" + selectedNode + "/" + value,
          showGraph);
   }
}

function updateFilterList() {
   get(urlPrefix+"view/activeFilters", function (list) {
      var parsedList = JSON.parse(list);
      setNewContent("filterTmpl", { entries: parsedList} , "filters");
   });
}

function getArcInfo(id) {
   get(urlPrefix+"view/arc/" + id, function (e) {
      var arcInfo = JSON.parse(e);
      setNewContent("arcInfo", arcInfo, "description");
   });
}

function openPage(pageName, elmnt) {
   var tabcontent = document.getElementsByClassName("tabcontent");
   for (var i = 0; i < tabcontent.length; i++) {
      tabcontent[i].style.opacity = 0;
      tabcontent[i].style.zIndex=-1;
   }

   var tablinks = document.getElementsByClassName("activetablink");
   for (var i = 0; i < tablinks.length; i++) {
      tablinks[i].className="tablink";
   }

   document.getElementById(pageName).style.opacity=100;
   document.getElementById(pageName).style.zIndex=1;
   elmnt.className="activetablink";
   elmnt.blur();
}

window.onload=function() {
   openPage("graph", document.getElementById("defaultOpen"))
};
