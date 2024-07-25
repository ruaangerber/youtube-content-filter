from mitmproxy import http
from mitmproxy import ctx
import requests
import json

class ChangeHTTPCode:
    filter = "youtubei/v1"

    def request(self, flow: http.HTTPFlow) -> None:
        ctx.log.info("Filtering URL: " + flow.request.pretty_url)
        
        if (flow.request.method == 'POST'):
            headers = {'Content-Type': 'application/json'}
            raw_json = json.loads(flow.request.text)
            body = {'url': raw_json['context']['client']['originalUrl']}
            filter_endpoint = "http://localhost:8081/filter"

            if (self.filter in flow.request.pretty_url):
                ctx.log.info("Sending URL to filter service: "+flow.request.pretty_url)
            
                response = requests.post(filter_endpoint, json=body)

                ctx.log.info("Response for url: " + flow.request.pretty_url + "; response: " + str(response))
                response_data = json.loads(response.text)
                ctx.log.info(response_data)
                ctx.log.info(response_data['allowed'])
                ctx.log.info(response_data['matches'])

                if (response.status_code != 200 or response_data['allowed'] == False):
                    ctx.log.info("Content blocked for url " + flow.request.pretty_url)
#                    flow.kill()
                    flow.response = http.Response.make(
                        403,
                        b"This request has been blocked by mitmproxy",
                        {"Content-Type": "text/plain"}
                    )
                    flow.intercept()

addons = [ChangeHTTPCode()]
