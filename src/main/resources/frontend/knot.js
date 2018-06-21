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


function setNewContent(templateId, data, targetId) {
   const tplInfo = _.template(document.getElementById(templateId).innerHTML);
   const target = document.getElementById(targetId);
   target.innerHTML = tplInfo(data);
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
}

function showPath(value) {
   var response = JSON.parse(value);
   setNewContent("classpathTmpl", {entries: response}, "classpath");
}

function showReport(value) {
   var report = JSON.parse(value);
   setNewContent("refReportTmpl", report, "refReport");
}

function stopMovingNodes(params) {
   network.setOptions({
      physics: false
   });
   zoomEnabled = true;
}

function showGraph(responseText) {
   var response = JSON.parse(responseText);
   var nodes = new vis.DataSet(response.nodes);
   var edges = new vis.DataSet(response.edges);

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
      }
   };
   network = new vis.Network(container, data, options);
   selectedNode = "";
   document.getElementById("description").innerHTML =
 "Click on node or arc to select it.";
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
         document.getElementById("description").innerHTML =
 "nothing selected";
      }
   });
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
      get(urlPrefix+"view/node/" + selectedNode + "/listmode/" + mode,
          showGraph);
   }
}

function setFilter(value) {
   get(urlPrefix+"view/filters/" + value, showGraph);
}

function impliedOnly(value) {
   get(urlPrefix+"view/filters/impliedBy/" + selectedNode + "/" + value,
       showGraph);
}

function getArcInfo(id) {
   get(urlPrefix+"view/arc/" + id, function (e) {
      var arcInfo = JSON.parse(e);
      setNewContent("arcInfo", arcInfo, "description");
   });
}
